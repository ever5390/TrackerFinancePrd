package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedMemberException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionSaveService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
public class TransactionSaveServiceImpl implements ITransactionSaveService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionSaveServiceImpl.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final MemberRepository memberRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final CategoryRepository categoryRepository;

    private final SegmentRepository segmentRepository;

    public TransactionSaveServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, MemberRepository memberRepository, PaymentMethodRepository paymentMethodRepository, CategoryRepository categoryRepository, SegmentRepository segmentRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.memberRepository = memberRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.categoryRepository = categoryRepository;
        this.segmentRepository = segmentRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction save(Transaction transactionRequest) throws CustomException, InsuficientFundsException, ObjectNotFoundException, UnspecifiedMemberException, AccountEqualsException {
        LOG.info("inicio transacción impl");

        //Setter params by default
        transactionRequest = settingDefaultParameters(transactionRequest);

        //Validation params
        validateFormatAndCorrectValueAmount(transactionRequest.getAmount());
        validCreateAt(transactionRequest.getCreateAt());
        transactionRequest.setCategory(validateCategory(transactionRequest));
        transactionRequest.setSegment(validateSegment(transactionRequest));
        transactionRequest.setPaymentMethod(validatePaymentMethod(transactionRequest.getPaymentMethod(), transactionRequest.getType(), "origen", transactionRequest.getUserId()));
        transactionRequest.setPaymentMethodDestiny(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())?null:validatePaymentMethod(transactionRequest.getPaymentMethodDestiny(), transactionRequest.getType(), "destino", transactionRequest.getUserId()));
        validateAccountBalanceAvailableForEnteredAmount(transactionRequest, transactionRequest.getPaymentMethod().getAccount());

        if (TypeEnum.LOAN.equals(transactionRequest.getType())) {
            //Valid counterparty member
            transactionRequest.setMember(validMemberForLoanTransaction(transactionRequest));
        }

        if (TypeEnum.PAYMENT.equals(transactionRequest.getType())) {
            //update loan assoc transaction in DB
            Transaction transactionLoanAssocUpdated = updateLoanAssocFromPaymentToSave(transactionRequest);
            transactionRepository.save(transactionLoanAssocUpdated);

            //Update Member to payment request
            transactionRequest.setMember(transactionLoanAssocUpdated.getMember());
        }

        if (!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())) {
            //proccessing balance account
            Long idAccount = transactionRequest.getPaymentMethod().getAccount().getId();
            Account account = accountRepository.findByIdAndUserId(idAccount, transactionRequest.getUserId());
            account.setCurrentBalance(getNewBalance(transactionRequest, account));

            //update balance account
            accountRepository.save(account);
        }

        if (TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())) {

            Account accountOrigin = transactionRequest.getPaymentMethod().getAccount();
            Account accountDestiny = transactionRequest.getPaymentMethodDestiny().getAccount();

            validationOfEqualsAccounts(accountOrigin, accountDestiny);

            double amountOriginAccount = accountOrigin.getCurrentBalance();
            double amountDestinyAccount = accountDestiny.getCurrentBalance();

            amountOriginAccount = amountOriginAccount - transactionRequest.getAmount();
            amountDestinyAccount = amountDestinyAccount + transactionRequest.getAmount();

            accountOrigin.setCurrentBalance(amountOriginAccount);
            accountDestiny.setCurrentBalance(amountDestinyAccount);

            accountRepository.save(accountOrigin);
            accountRepository.save(accountDestiny);
        }

        //register transactionRequest
        return transactionRepository.save(transactionRequest);

    }

    private void validateAccountBalanceAvailableForEnteredAmount(Transaction transactionRequest, Account account) throws InsuficientFundsException {
        if (transactionRequest.getAction().equals(ActionEnum.REALICÉ) && transactionRequest.getAmount() > account.getCurrentBalance())  throw new InsuficientFundsException("Saldo no disponible en la cuenta origen para esta operación");
    }

    private void validationOfEqualsAccounts(Account accountOrigin, Account accountDestiny) throws AccountEqualsException {
        if(accountOrigin.getId().equals(accountDestiny.getId())) {
            throw new AccountEqualsException("Las cuentas origen y destino no pueden ser las mismas.");
        }
    }

    private void validCreateAt(LocalDateTime createAtReq) throws CustomException {
        if(createAtReq == null || createAtReq.isAfter(LocalDateTime.now())) {
            throw new CustomException("Ingrese una fecha válida no mayor a la fecha actual");
        }
    }

    private void validateFormatAndCorrectValueAmount(double amount) throws CustomException {
        LOG.info("Amount: " + amount);
        Double.parseDouble(String.valueOf(amount));
        LOG.info("Amount post parseDouble: " + amount);
        if (amount <= 0)  throw new CustomException("El monto de la operación debe ser mayor a cero.");
    }

    private PaymentMethod validatePaymentMethod(PaymentMethod paymentMethodReq, TypeEnum typeOperation, String typePaymentMethod, Long userId) throws ObjectNotFoundException {

        String originOrDestiny = TypeEnum.TRANSFERENCE.equals(typeOperation)?typePaymentMethod:"";

        if(paymentMethodReq == null || StringUtils.isEmpty(paymentMethodReq.getId().toString())) {
            throw new ObjectNotFoundException("El método de pago " + originOrDestiny + " no ha sido encontrado");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(paymentMethodReq.getId(), userId);
        if(paymentMethod == null) {
            throw new ObjectNotFoundException("El método de pago " + originOrDestiny  + " no ha sido encontrado");
        }

        return paymentMethod;
    }

    private Category validateCategory(Transaction transactionRequest) throws ObjectNotFoundException {

        if(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType()) && transactionRequest.getCategory() != null && transactionRequest.getCategory().getId() != null && transactionRequest.getCategory().getId() != 0 ) {
            Category category = categoryRepository.findByIdAndUserId(transactionRequest.getCategory().getId(), transactionRequest.getUserId());
            if(category == null) {
                throw new ObjectNotFoundException("La categoría no ha sido encontrado");
            }
            return category;
        }

        return null;
    }

    private Segment validateSegment(Transaction transactionRequest) throws ObjectNotFoundException {
        if(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType()) && transactionRequest.getSegment() != null && transactionRequest.getSegment().getId() != null && transactionRequest.getSegment().getId() != 0) {
            Segment segment = segmentRepository.findByIdAndUserId(transactionRequest.getSegment().getId(), transactionRequest.getUserId());
            if(segment == null ) {
                throw new ObjectNotFoundException("El segmento no ha sido encontrado");
            }
            return segment;
        }

        return null;
    }

    private Transaction settingDefaultParameters(Transaction transactionRequest) throws CustomException {

        if(transactionRequest.getType() == null) throw new CustomException("Por favor seleccione el tipo de operación para poder procesar la operación");

        if (transactionRequest.getType().equals(TypeEnum.TRANSFERENCE)) {
            transactionRequest.setBlock(BlockEnum.NOT_APPLICABLE);
            transactionRequest.setAction(ActionEnum.REALICÉ);
            transactionRequest.setRemaining(0);
            transactionRequest.setMember(null);
            transactionRequest.setCategory(null);
            transactionRequest.setSegment(null);
        }

        if (TypeEnum.LOAN.equals(transactionRequest.getType())) {
            transactionRequest.setStatus(StatusEnum.PENDING);
            transactionRequest.setRemaining(transactionRequest.getAmount());
        }

        //For all types other than Payment : loanAssoc = null
        if (!TypeEnum.PAYMENT.equals(transactionRequest.getType())) {
            transactionRequest.setIdLoanAssoc(null);
        }

        //For all types other than LOAN
        if (!TypeEnum.LOAN.equals(transactionRequest.getType())) {
            transactionRequest.setStatus(StatusEnum.NOT_APPLICABLE);
            transactionRequest.setRemaining(0);
            transactionRequest.setMember(null);
        }

        //Updating action tx
        if (transactionRequest.getType().equals(TypeEnum.EXPENSE)) {
            transactionRequest.setAction(ActionEnum.REALICÉ);
        }

        if (transactionRequest.getType().equals(TypeEnum.INCOME)) {
            transactionRequest.setAction(ActionEnum.RECIBÍ);
        }

        if(transactionRequest.getType().equals(TypeEnum.PAYMENT) || transactionRequest.getType().equals(TypeEnum.LOAN)) {
            if(transactionRequest.getAction() == null) throw new CustomException("Por favor seleccione la acción para poder procesar la operación");
        }

        //Updating Block tx : IN - OUT
        if (transactionRequest.getAction().equals(ActionEnum.REALICÉ) && !transactionRequest.getType().equals(TypeEnum.TRANSFERENCE)) {
            transactionRequest.setBlock(BlockEnum.OUT);
        }

        if (transactionRequest.getAction().equals(ActionEnum.RECIBÍ)) {
            transactionRequest.setBlock(BlockEnum.IN);
        }

        return transactionRequest;
    }

    private Member validMemberForLoanTransaction(Transaction transactionRequest) throws UnspecifiedMemberException {

        String message = transactionRequest.getAction().equals(ActionEnum.RECIBÍ)?" de quien se recibió ": "a quien se otorgó ";
        if (transactionRequest.getMember() == null ) throw new UnspecifiedMemberException("Ocurrió un error al intentar detectar a la persona " + message + "el préstamo.");
        Member counterParty =  memberRepository.findByIdAndUserId(transactionRequest.getMember().getId(), transactionRequest.getUserId());
        if(counterParty == null) throw new UnspecifiedMemberException("Ocurrió un error al intentar detectar a la persona " + message + "el préstamo.");
        return counterParty;
    }

    private Transaction updateLoanAssocFromPaymentToSave(Transaction transactionRequest) throws ObjectNotFoundException, CustomException {

            Long idTransactionLoanAssoc = transactionRequest.getIdLoanAssoc();
            if (idTransactionLoanAssoc == null) throw new ObjectNotFoundException("El préstamo al que hace referencia el pago registrado no existe.");
            Transaction transactionLoanAssoc = transactionRepository.findByIdAndUserId(idTransactionLoanAssoc, transactionRequest.getUserId());
            if(transactionLoanAssoc == null) new ObjectNotFoundException("El préstamo al que hace referencia el pago registrado no existe.");

            if(!transactionLoanAssoc.getType().equals(TypeEnum.LOAN)) {
                throw new CustomException("La transacción seleccionada como préstamo no es correcto, seleccione otro.");
            }

            if(transactionRequest.getAction().equals(transactionLoanAssoc.getAction())) {
                throw new CustomException("La acción seleccionada para el pago y el préstamo son iguales(" + transactionRequest.getAction() + ") y deberían ser opuestos.");
            }

            double currentRemainingLoanAssoc = transactionLoanAssoc.getRemaining();
            double newRemainingLoanAssoc = currentRemainingLoanAssoc - transactionRequest.getAmount();

            if(newRemainingLoanAssoc < 0) {
                throw new CustomException("El monto registrado en el pago supera al monto pendiente de pago " + currentRemainingLoanAssoc);
            }

            //update status if remaining is zero
            if(newRemainingLoanAssoc == 0) {
                transactionLoanAssoc.setStatus(StatusEnum.PAYED);
            }

            //update remaining loan assoc
            transactionLoanAssoc.setRemaining(newRemainingLoanAssoc);


            return transactionLoanAssoc;

    }

    private static double getNewBalance(Transaction transactionRequest, Account account) throws InsuficientFundsException {
        double currentBalance =  account.getCurrentBalance();
        double newBalance = 0.0;

        if(BlockEnum.IN.equals(transactionRequest.getBlock())) {
            newBalance = currentBalance + transactionRequest.getAmount();
        }

        if(BlockEnum.OUT.equals(transactionRequest.getBlock())) {
            newBalance = currentBalance - transactionRequest.getAmount();
        }

        if(newBalance < 0) {
            throw new InsuficientFundsException("Saldo insuficiente para efectuar esta transacción");
        }

        return newBalance;
    }


}
