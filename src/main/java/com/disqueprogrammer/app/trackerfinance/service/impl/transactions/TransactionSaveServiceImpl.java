package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedCounterpartException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.*;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionSaveService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Transactional
@Service
public class TransactionSaveServiceImpl implements ITransactionSaveService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionSaveServiceImpl.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final CounterpartRepository counterpartRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final SubCategoryRepository subCategoryRepository;

    private final RecurringRepository recurringRepository;

    public TransactionSaveServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, CounterpartRepository counterpartRepository, PaymentMethodRepository paymentMethodRepository, CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository, RecurringRepository recurringRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.counterpartRepository = counterpartRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.recurringRepository = recurringRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction save(Transaction transactionRequest) throws CustomException, InsuficientFundsException, UnspecifiedCounterpartException, AccountEqualsException {

        settingDefaultParameters(transactionRequest);
        validationAmount(transactionRequest);
        validCreateAt(transactionRequest.getCreateAt());
        transactionRequest.setSubCategory((validateCategory(transactionRequest)));
        transactionRequest.setAccount(validateOnlyAccount(transactionRequest.getAccount()));
        transactionRequest.setAccountDestiny(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())?null:validateTransferAccount(transactionRequest.getAccountDestiny(), transactionRequest.getType(), "destino", transactionRequest.getWorkspaceId()));
        transactionRequest.setPaymentMethod((transactionRequest.getPaymentMethod() == null || transactionRequest.getPaymentMethod().getId() == 0)? null: validateonlyPaymentMethod(transactionRequest.getPaymentMethod()));
        transactionRequest.setPaymentMethodDestiny(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())?null:validateTransferPaymentMethod(transactionRequest.getPaymentMethodDestiny(), transactionRequest.getType(), "destino", transactionRequest.getWorkspaceId()));
        validateAccountBalanceAvailableForEnteredAmount(transactionRequest, transactionRequest.getAccount());

        if (TypeEnum.LOAN.equals(transactionRequest.getType())) {
            //Valid counterparty counterpart
            transactionRequest.setCounterpart(validCounterpartForLoanTransaction(transactionRequest));
        }

        if( TypeEnum.LOAN.equals(transactionRequest.getType())
                || (TypeEnum.EXPENSE.equals(transactionRequest.getType()) && transactionRequest.getAccount().getCardType().isFixedParameter())
                || (TypeEnum.TRANSFERENCE.equals(transactionRequest.getType()) && transactionRequest.getAccount().getCardType().isFixedParameter()) ) {
            transactionRequest.setStatus(StatusEnum.PENDING);
            transactionRequest.setRemaining(transactionRequest.getAmount());
        }

        if (TypeEnum.PAYMENT.equals(transactionRequest.getType())) {
            //update loan assoc transaction in DB
            Transaction transactionLoanAssocUpdated = updateLoanAssocFromPaymentToSave(transactionRequest);
            transactionRepository.save(transactionLoanAssocUpdated);

            transactionRequest.setTransactionLoanAssocToPay(transactionLoanAssocUpdated);
            //Update Counterpart to payment request
            transactionRequest.setCounterpart(transactionLoanAssocUpdated.getCounterpart());
        }

        if (!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())) {
            //proccessing balance account
            Long idAccount = transactionRequest.getAccount().getId();
            Account account = accountRepository.findByIdAndWorkspaceId(idAccount, transactionRequest.getWorkspaceId());
            account.setCurrentBalance(getNewBalance(transactionRequest, account));

            //update balance account
            accountRepository.save(account);
        }

        if (TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())) {

            Account accountOrigin = transactionRequest.getAccount();
            Account accountDestiny = transactionRequest.getAccountDestiny();

            validationOfEqualsAccounts(accountOrigin, accountDestiny);

            BigDecimal amountOriginAccount = accountOrigin.getCurrentBalance();
            BigDecimal amountDestinyAccount = accountDestiny.getCurrentBalance();

            amountOriginAccount = amountOriginAccount.subtract(transactionRequest.getAmount());
            amountDestinyAccount = amountDestinyAccount.add(transactionRequest.getAmount());

            accountOrigin.setCurrentBalance(amountOriginAccount.setScale(2, RoundingMode.HALF_UP));
            accountDestiny.setCurrentBalance(amountDestinyAccount.setScale(2, RoundingMode.HALF_UP));

            accountRepository.save(accountOrigin);
            accountRepository.save(accountDestiny);
        }

        // Case recurring
        if(transactionRequest.getRecurring() != null) {
            transactionRequest.getRecurring().setStatusIsPayed(true);
            generateNextTransactionWithRecurring(transactionRequest);
        }

        return transactionRepository.save(transactionRequest);
    }

    private Account validateOnlyAccount(Account account) throws CustomException {

        if(account == null || account.getId()== 0) {
            throw new CustomException("Seleccione una cuenta.");
        }

        Account accounFound = accountRepository.findByIdAndWorkspaceId(account.getId(), account.getWorkspaceId());
        if(accounFound == null) {
            throw new CustomException("La cuenta seleccionada no ha sido encontrada.");
        }

        return accounFound;
    }

    private void validateAccountBalanceAvailableForEnteredAmount(Transaction transactionRequest, Account account) throws InsuficientFundsException {
        int comparisonResult = transactionRequest.getAmount().compareTo(account.getCurrentBalance());
        if (transactionRequest.getAction().equals(ActionEnum.REALICÉ) && comparisonResult > 0)  throw new InsuficientFundsException("Saldo no disponible en la cuenta origen para esta operación");
    }

    private void validationOfEqualsAccounts(Account accountOrigin, Account accountDestiny) throws AccountEqualsException {
        if(Objects.equals(accountOrigin.getId(), accountDestiny.getId())) {
            throw new AccountEqualsException("Las cuentas origen y destino no pueden ser las mismas.");
        }
    }

    private void validCreateAt(LocalDateTime createAtReq) throws CustomException {
        if(createAtReq == null || createAtReq.isAfter(LocalDateTime.now())) {
            throw new CustomException("Ingrese una fecha válida no mayor a la fecha actual");
        }
    }

    private void validationAmount(Transaction transactionReq) throws CustomException {
        BigDecimal zero = BigDecimal.ZERO;
        int comparisonResult = transactionReq.getAccount().getCurrentBalance().compareTo(zero);
        if(comparisonResult <= 0)
            throw new CustomException("El monto de la operación debe ser mayor a cero.");
    }

    private Account validateTransferAccount(Account accountReq, TypeEnum typeOperation, String operationType, Long workspaceId) throws CustomException {

        String originOrDestiny = TypeEnum.TRANSFERENCE.equals(typeOperation)?operationType:"";

        if(accountReq == null || StringUtils.isEmpty(accountReq.getId().toString())) {
            throw new CustomException("Seleccione una cuenta " + originOrDestiny + " por favor");
        }

        Account accountFound = accountRepository.findByIdAndWorkspaceId(accountReq.getId(), workspaceId);
        if(accountFound == null) {
            throw new CustomException("La cuenta " + originOrDestiny  + " seleccionada no ha sido encontrado");
        }

        return accountFound;
    }

    private PaymentMethod validateonlyPaymentMethod(PaymentMethod paymentMethodReq) throws CustomException {


        if(paymentMethodReq == null || paymentMethodReq.getId() == 0 || StringUtils.isEmpty(paymentMethodReq.getId().toString())) {
            return null;
        }

        PaymentMethod paymentMethodFound = paymentMethodRepository.findByIdAndWorkspaceId(paymentMethodReq.getId(), paymentMethodReq.getWorkspaceId());
        if(paymentMethodFound == null) {
            throw new CustomException("El método de pago no ha sido encontrado");
        }

        return paymentMethodFound;
    }

    private PaymentMethod validateTransferPaymentMethod(PaymentMethod paymentMethodReq, TypeEnum typeOperation, String typePaymentMethod, Long workspaceId) throws CustomException {

        String originOrDestiny = TypeEnum.TRANSFERENCE.equals(typeOperation)?typePaymentMethod:"";

        if(paymentMethodReq == null || paymentMethodReq.getId() == 0 || StringUtils.isEmpty(paymentMethodReq.getId().toString())) {
            return null;
        }

        PaymentMethod paymentMethodFound = paymentMethodRepository.findByIdAndWorkspaceId(paymentMethodReq.getId(), workspaceId);
        if(paymentMethodFound == null) {
            throw new CustomException("El método de pago " + originOrDestiny  + " no ha sido encontrado");
        }

        return paymentMethodFound;
    }

    private SubCategory validateCategory(Transaction transactionRequest) throws CustomException {

        if(TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())) {
            return null;
        }

        if(transactionRequest.getSubCategory() == null || transactionRequest.getSubCategory().getId()==null || transactionRequest.getSubCategory().getId() == 0) {
            throw new CustomException("Seleccione una categoría por favor.");
        }

        SubCategory subCategory = subCategoryRepository.findByIdAndWorkspaceId(transactionRequest.getSubCategory().getId(), transactionRequest.getWorkspaceId());
        if(subCategory == null) {
            throw new CustomException("La categoría no ha sido encontrada, pruebe nuevamente");
        }
        return subCategory;

    }

    private void settingDefaultParameters(Transaction transactionRequest) throws CustomException {

        if(transactionRequest.getType() == null) throw new CustomException("Por favor seleccione el tipo de operación para poder procesar la operación");

        if (transactionRequest.getType().equals(TypeEnum.TRANSFERENCE)) {
            transactionRequest.setBlock(BlockEnum.NOT_APPLICABLE);
            transactionRequest.setAction(ActionEnum.NOT_APPLICABLE);
            transactionRequest.setRemaining(BigDecimal.ZERO);
            transactionRequest.setCounterpart(null);
            transactionRequest.setSubCategory(null);
        }

        //For all types other than LOAN
        if (!TypeEnum.LOAN.equals(transactionRequest.getType())) {
            transactionRequest.setStatus(StatusEnum.NOT_APPLICABLE);
            transactionRequest.setRemaining(BigDecimal.ZERO);
            transactionRequest.setCounterpart(null);
        }

        //For all types other than Payment : loanAssoc = null
        if (!TypeEnum.PAYMENT.equals(transactionRequest.getType())) {
            transactionRequest.setTransactionLoanAssocToPay(null);
        }

        //Updating action tx
        if (transactionRequest.getType().equals(TypeEnum.EXPENSE)) {
            transactionRequest.setAction(ActionEnum.REALICÉ);
        }

        if (transactionRequest.getType().equals(TypeEnum.INCOME)) {
            transactionRequest.setAction(ActionEnum.RECIBÍ);
        }

        if(transactionRequest.getType().equals(TypeEnum.PAYMENT) || transactionRequest.getType().equals(TypeEnum.LOAN)) {
            if(transactionRequest.getAction() == null) throw new CustomException("Se produjo un error al procesar el tipo de opreación, por favor inténtelo nuevamente.");
        }

        //Updating Block tx : IN - OUT
        if (transactionRequest.getAction().equals(ActionEnum.REALICÉ) && !transactionRequest.getType().equals(TypeEnum.TRANSFERENCE)) {
            transactionRequest.setBlock(BlockEnum.OUT);
        }

        if (transactionRequest.getAction().equals(ActionEnum.RECIBÍ)) {
            transactionRequest.setBlock(BlockEnum.IN);
        }

    }

    private Transaction validTxLoanAssocToPayType(Transaction transactionLoanAssocToPay) throws CustomException {
        if(transactionLoanAssocToPay == null || transactionLoanAssocToPay.getTransactionLoanAssocToPay().getId() == null)
            throw new CustomException("Es necesario que seleccione la operación de préstamo al que hace referencia el pago que estás registrando");

        return transactionRepository.findById(transactionLoanAssocToPay.getId()).orElseThrow(()-> new CustomException("No se ecnontró el préstamos que seleccionó como referencia para su pago a realizar, seleccione nuevamente."));
    }

    private Counterpart validCounterpartForLoanTransaction(Transaction transactionRequest) throws UnspecifiedCounterpartException {

        String message = transactionRequest.getAction().equals(ActionEnum.RECIBÍ)?" de quien se recibió ": "a quien se otorgó ";
        if (transactionRequest.getCounterpart() == null ) throw new UnspecifiedCounterpartException("Ocurrió un error al intentar detectar a la persona " + message + "el préstamo.");
        Counterpart counterParty =  counterpartRepository.findByIdAndWorkspaceId(transactionRequest.getCounterpart().getId(), transactionRequest.getWorkspaceId());
        if(counterParty == null) throw new UnspecifiedCounterpartException("Ocurrió un error al intentar detectar a la persona " + message + "el préstamo.");
        return counterParty;
    }

    private Transaction updateLoanAssocFromPaymentToSave(Transaction transactionRequest) throws CustomException {

            if (transactionRequest.getTransactionLoanAssocToPay() == null || transactionRequest.getTransactionLoanAssocToPay().getId() == 0)
                throw new CustomException("El préstamo al que hace referencia el pago registrado no existe.");

            Long idTransactionLoanAssoc = transactionRequest.getTransactionLoanAssocToPay().getId();
            Transaction transactionLoanAssoc = transactionRepository.findByIdAndWorkspaceId(idTransactionLoanAssoc, transactionRequest.getWorkspaceId());
            if(transactionLoanAssoc == null)
                throw new CustomException("El préstamo al que hace referencia el pago registrado no existe.");

        validateTransactionAssoc(transactionLoanAssoc);

        if(transactionRequest.getAction().equals(transactionLoanAssoc.getAction()) && transactionLoanAssoc.getType().equals(TypeEnum.LOAN)) {
                throw new CustomException("La acción seleccionada para el pago y el préstamo son iguales(" + transactionRequest.getAction() + ") y deberían ser opuestos.");
            }

            BigDecimal currentRemainingLoanAssoc = transactionLoanAssoc.getRemaining();
            BigDecimal newRemainingLoanAssoc = currentRemainingLoanAssoc.subtract(transactionRequest.getAmount());

            int comparisonResult = newRemainingLoanAssoc.compareTo(BigDecimal.ZERO);

            if(comparisonResult < 0) {
                throw new CustomException("El monto registrado en el pago supera al monto pendiente de pago " + currentRemainingLoanAssoc);
            }

            //update status if remaining is zero
            if(comparisonResult == 0) {
                transactionLoanAssoc.setStatus(StatusEnum.PAYED);
            }

            //update remaining loan assoc
            transactionLoanAssoc.setRemaining(newRemainingLoanAssoc);


            return transactionLoanAssoc;

    }

    private static void validateTransactionAssoc(Transaction transactionLoanAssoc) throws CustomException {
        LOG.info("transactionLoanAssoc encontrada:");
        LOG.info(transactionLoanAssoc.toString());
        //Solo los LOAN || (EXPENSE + TC) || TRANSFER + TC ORIGEN pasas, sino: mensaje error.
        if(!TypeEnum.LOAN.equals(transactionLoanAssoc.getType())
                && !TypeEnum.EXPENSE.equals(transactionLoanAssoc.getType())
                && !TypeEnum.TRANSFERENCE.equals(transactionLoanAssoc.getType()) ) {
            throw new CustomException("La transacción seleccionada asociada al registro de pago no concuerda con un tipo de operación pendiente válido, seleccione nuevamente.");
        }

        if(!transactionLoanAssoc.getType().equals(TypeEnum.LOAN) && !transactionLoanAssoc.getAccount().getCardType().isFixedParameter() ) {
            throw new CustomException("La transacción seleccionada asociada al registro de pago no concuerda con un tipo de operación pendiente válido, seleccione nuevamente.");
        }
    }

    private static BigDecimal getNewBalance(Transaction transactionRequest, Account account) throws InsuficientFundsException {

        BigDecimal currentBalance =  account.getCurrentBalance();
        BigDecimal newBalance = BigDecimal.ZERO;

        if(BlockEnum.IN.equals(transactionRequest.getBlock())) {
            newBalance = currentBalance.add(transactionRequest.getAmount());
        }

        if(BlockEnum.OUT.equals(transactionRequest.getBlock())) {
            newBalance = currentBalance.subtract(transactionRequest.getAmount());
        }

        int comparisonResult = newBalance.compareTo(BigDecimal.ZERO);
        if(comparisonResult <= 0) {
            throw new InsuficientFundsException("Saldo insuficiente para efectuar esta transacción");
        }

        return newBalance.setScale(2, RoundingMode.HALF_UP);
    }   

    @Override
    public void saveNewTransactionRecurring(Transaction nextTransactionRecurring) throws Exception {
        try {
            Recurring recurring = nextTransactionRecurring.getRecurring();
            saveNewTransactionRecurringByDateCreatedTx(nextTransactionRecurring, recurring, LocalDateTime.now());
        } catch (Exception error) {
            LOG.error(error.getLocalizedMessage());
            throw new Exception("Ocurrió un error al procesar la operación a registrar.");
        }

    }

    private void saveNewTransactionRecurringByDateCreatedTx(Transaction nextTransactionRecurring, Recurring recurring, LocalDateTime localDateTimeReq) {
        for (int selectedCalendar : recurring.getItemDateSelectedPerPeriod()) {

            Recurring newRecurring = new Recurring();

            if (recurring.getPeriod().equals(PeriodEnum.DAY)) {
                newRecurring.setItemDateSelectedPerPeriod(getItemSelectedPerPeriod(selectedCalendar));
                newRecurring.setNextPaymentDate(localDateTimeReq.plusDays(recurring.getNumberOfTimes()));
                newRecurring.setNextClosestPaymentDate(recurring.getNextPaymentDate());
            }

            if (recurring.getPeriod().equals(PeriodEnum.WEEK)) {
                //Lunes = 1, Martes = 2, ..., Domingo = 7
                int dayWeekByActualDate = localDateTimeReq.getDayOfWeek().getValue();
                int daysRemainingUntilTheWeekDayRequired = 0;
                if (dayWeekByActualDate <= (selectedCalendar + 1)) {
                    daysRemainingUntilTheWeekDayRequired = (selectedCalendar + 1) - dayWeekByActualDate;
                } else {
                    daysRemainingUntilTheWeekDayRequired = (7 - dayWeekByActualDate) + (selectedCalendar + 1);
                }

                newRecurring.setItemDateSelectedPerPeriod(getItemSelectedPerPeriod(selectedCalendar));
                newRecurring.setNextClosestPaymentDate(localDateTimeReq.plusDays(daysRemainingUntilTheWeekDayRequired));
                newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusDays(newRecurring.getNumberOfTimes() * 7L));
            }

            if (recurring.getPeriod().equals(PeriodEnum.MONTH)) {
                int daysRemainingUntilTheDayRequired = 0;

                if (localDateTimeReq.getDayOfMonth() <= (selectedCalendar)) {

                    if ((selectedCalendar == 31 || selectedCalendar == 30) && localDateTimeReq.toLocalDate().lengthOfMonth() < selectedCalendar) {
                        daysRemainingUntilTheDayRequired = localDateTimeReq.toLocalDate().lengthOfMonth() - localDateTimeReq.getDayOfMonth();
                    } else {
                        daysRemainingUntilTheDayRequired = (selectedCalendar) - localDateTimeReq.getDayOfMonth();
                    }

                } else {
                    //Get final day of actual month
                    daysRemainingUntilTheDayRequired = (localDateTimeReq.toLocalDate().lengthOfMonth() - localDateTimeReq.getDayOfMonth()) + (selectedCalendar + 1);
                }

                newRecurring.setItemDateSelectedPerPeriod(getItemSelectedPerPeriod(selectedCalendar));
                newRecurring.setNextClosestPaymentDate(localDateTimeReq.plusDays(daysRemainingUntilTheDayRequired));
                newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusMonths(recurring.getNumberOfTimes()));
            }

            if (recurring.getPeriod().equals(PeriodEnum.YEAR)) {
                int daysRemainingUntilTheDayMonthRequired = 0;

                int anio = localDateTimeReq.getYear();
                LocalDateTime dateSelected = getDateTimeCreatedNewRecurringByPeriodYear(selectedCalendar, recurring, anio);

                if (localDateTimeReq.isBefore(dateSelected)) {
                    daysRemainingUntilTheDayMonthRequired = (int) ChronoUnit.DAYS.between(dateSelected, localDateTimeReq);
                } else {
                    daysRemainingUntilTheDayMonthRequired = (int) ChronoUnit.DAYS.between(dateSelected.plusYears(1), localDateTimeReq);
                }

                newRecurring.setItemDateSelectedPerPeriod(getItemSelectedPerPeriod(selectedCalendar));
                newRecurring.setNextClosestPaymentDate(localDateTimeReq.plusDays(daysRemainingUntilTheDayMonthRequired));
                newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusYears(recurring.getNumberOfTimes()));
            }

            newRecurring.setNumberOfTimes(recurring.getNumberOfTimes());
            newRecurring.setCode(String.valueOf(UUID.randomUUID()));
            newRecurring.setStatusIsPayed(false);
            newRecurring.setPeriod(recurring.getPeriod());
            transactionRepository.save(setterNewTransaction(nextTransactionRecurring, this.recurringRepository.save(newRecurring)));

        }
    }

    private static LocalDateTime getDateTimeCreatedNewRecurringByPeriodYear(int selectedCalendar, Recurring recurring, int anio) {
        int mes = selectedCalendar + 1;
        int dia = recurring.getDayMonth(); // Only YEAR period;

        //Only the month has fewer days than the selected day.
        if ((dia == 31 || dia == 30) && LocalDate.of(anio, mes, 1).lengthOfMonth() < dia) {
            dia = LocalDate.of(anio, mes, 1).lengthOfMonth();
        }

        LocalDate fecha = LocalDate.of(anio, mes, dia);
        LocalTime hora = LocalTime.of(12, 0); // Ejemplo: 12:00
        return LocalDateTime.of(fecha, hora);
    }

    private static List<Integer> getItemSelectedPerPeriod(int selectedCalendar) {
        List<Integer> itemDateSelectedPerPeriod = new ArrayList<>();
        itemDateSelectedPerPeriod.add(selectedCalendar);
        return itemDateSelectedPerPeriod;
    }

    private Transaction setterNewTransaction(Transaction transactionReq, Recurring recurring) {
        Transaction nextTransactionRecurring = new Transaction();
        nextTransactionRecurring.setId(transactionReq.getId());
        nextTransactionRecurring.setRecurring(recurring);
        nextTransactionRecurring.setAmount(transactionReq.getAmount());
        nextTransactionRecurring.setAction(transactionReq.getAction());
        nextTransactionRecurring.setBlock(transactionReq.getBlock());
        nextTransactionRecurring.setCounterpart(transactionReq.getCounterpart());
        nextTransactionRecurring.setDescription(transactionReq.getDescription());
        nextTransactionRecurring.setTags(transactionReq.getTags());
        nextTransactionRecurring.setType(transactionReq.getType());
        nextTransactionRecurring.setSubCategory(transactionReq.getSubCategory());
        nextTransactionRecurring.setStatus(transactionReq.getStatus());
        nextTransactionRecurring.setCreateAt(transactionReq.getCreateAt());
        nextTransactionRecurring.setTransactionLoanAssocToPay(transactionReq.getTransactionLoanAssocToPay());
        nextTransactionRecurring.setAccount(transactionReq.getAccount());
        nextTransactionRecurring.setAccountDestiny(transactionReq.getAccountDestiny());
        nextTransactionRecurring.setPaymentMethod(transactionReq.getPaymentMethod());
        nextTransactionRecurring.setPaymentMethodDestiny(transactionReq.getPaymentMethodDestiny());
        nextTransactionRecurring.setCounterpart(transactionReq.getCounterpart());
        nextTransactionRecurring.setResponsableUser(transactionReq.getResponsableUser());
        nextTransactionRecurring.setWorkspaceId(transactionReq.getWorkspaceId());
        return nextTransactionRecurring;
    }

    private void generateNextTransactionWithRecurring(Transaction transactionRequest) {
        // Duplicated tx with next Payment date
        Recurring newRecurring = new Recurring();
        newRecurring.setCode(transactionRequest.getRecurring().getCode());
        newRecurring.setPeriod(transactionRequest.getRecurring().getPeriod());
        newRecurring.setDayMonth(transactionRequest.getRecurring().getDayMonth());
        newRecurring.setItemDateSelectedPerPeriod(transactionRequest.getRecurring().getItemDateSelectedPerPeriod());
        newRecurring.setStatusIsPayed(false);
        newRecurring.setNumberOfTimes(transactionRequest.getRecurring().getNumberOfTimes());
        newRecurring.setNextClosestPaymentDate(transactionRequest.getRecurring().getNextPaymentDate());

        if(transactionRequest.getRecurring().getPeriod().equals(PeriodEnum.DAY)) {
            newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusDays(newRecurring.getNumberOfTimes()));
        }

        if(transactionRequest.getRecurring().getPeriod().equals(PeriodEnum.WEEK)) {
            newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusDays(newRecurring.getNumberOfTimes()* 7L));
        }

        int numberOfTimes = transactionRequest.getRecurring().getNumberOfTimes();

        if(transactionRequest.getRecurring().getPeriod().equals(PeriodEnum.MONTH)) {
            int dayMonthSelected = transactionRequest.getRecurring().getItemDateSelectedPerPeriod().get(0);
            int lengthOfMonth = newRecurring.getNextClosestPaymentDate().plusMonths(numberOfTimes).toLocalDate().lengthOfMonth();
            int yearPerPeriodMonth = newRecurring.getNextClosestPaymentDate().plusMonths(numberOfTimes).getYear();
            int monthPerPeriodMonth = newRecurring.getNextClosestPaymentDate().plusMonths(numberOfTimes).getMonthValue();

            if((dayMonthSelected == 30 || dayMonthSelected == 31)
                    &&  lengthOfMonth <  dayMonthSelected){

                LocalDate newNextPaymentDate = LocalDate.of(yearPerPeriodMonth, monthPerPeriodMonth, lengthOfMonth);
                LocalTime hour = LocalTime.of(12, 0);
                LocalDateTime dateSelected = LocalDateTime.of(newNextPaymentDate, hour);

                newRecurring.setNextPaymentDate(dateSelected);
            } else {
                newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusMonths(numberOfTimes));
            }
        }

        if(transactionRequest.getRecurring().getPeriod().equals(PeriodEnum.YEAR)) {
            int lengthOfMonthPerYear = newRecurring.getNextClosestPaymentDate().plusYears(numberOfTimes).toLocalDate().lengthOfMonth();

            if((transactionRequest.getRecurring().getDayMonth() == 30 || transactionRequest.getRecurring().getDayMonth() == 31)
                    &&  lengthOfMonthPerYear <  transactionRequest.getRecurring().getDayMonth()){

                LocalDateTime dateSelected = getDateTimeRegenerationRecurringPerPeriodYear(transactionRequest, newRecurring, numberOfTimes);

                newRecurring.setNextPaymentDate(dateSelected);
            } else {
                newRecurring.setNextPaymentDate(newRecurring.getNextClosestPaymentDate().plusYears(numberOfTimes));
            }
        }

        transactionRepository.save(setterNewTransaction(transactionRequest, this.recurringRepository.save(newRecurring)));
    }

    private static LocalDateTime getDateTimeRegenerationRecurringPerPeriodYear(Transaction transactionRequest, Recurring newRecurring, int numberOfTimes) {
        int lengthOfMonth = newRecurring.getNextClosestPaymentDate().plusYears(numberOfTimes).toLocalDate().lengthOfMonth();
        int yearPerPeriodYear = newRecurring.getNextClosestPaymentDate().plusYears(numberOfTimes).getYear();
        int monthPerPeriodYear = transactionRequest.getRecurring().getItemDateSelectedPerPeriod().get(0);
        LocalDate newNextPaymentDate = LocalDate.of(yearPerPeriodYear, monthPerPeriodYear, lengthOfMonth);
        LocalTime hour = LocalTime.of(12, 0);
        return LocalDateTime.of(newNextPaymentDate, hour);
    }

}
