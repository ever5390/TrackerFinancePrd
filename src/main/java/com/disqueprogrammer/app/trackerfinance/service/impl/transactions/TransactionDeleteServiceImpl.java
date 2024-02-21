package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionDeleteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class TransactionDeleteServiceImpl implements ITransactionDeleteService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionDeleteServiceImpl.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    public TransactionDeleteServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void delete(Long transactionRequestId, Long userId) throws CustomException, ObjectNotFoundException, InsuficientFundsException {

        //Exist Tx
        Transaction transactionFounded = transactionRepository.findByIdAndUserId(transactionRequestId, userId);
        if (transactionFounded == null) throw new ObjectNotFoundException("No se encontró la transacción seleccionada");

        if (!TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
            reverseAndUpDateBalanceAvailableAccountByDeleteTx(transactionFounded);
        }

        if (TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
            reverseAndUpdateBalanceAvailableAccountOriginAndDestinyByDeleteTx(transactionFounded);
        }

        if (TypeEnum.LOAN.equals(transactionFounded.getType())) {
            //Searching for transactions Payment type associated with this operation
            List<Transaction> paymentTransaction = transactionRepository.findPaymentsByLoanIdAssocAndUserId(transactionRequestId, transactionFounded.getUserId());
            if(!paymentTransaction.isEmpty())  throw new CustomException("No es posible eliminar la transacción de tipo PRÉSTAMO ya que se encontraron pagos asociados a ella.");
        }

        if (TypeEnum.PAYMENT.equals(transactionFounded.getType())) {
            Transaction transactionLoanUpdated = updateLoanAssocReverseByDeleteTx(transactionFounded);
            //update loan transaction in DB
            transactionRepository.save(transactionLoanUpdated);
        }

        //delete transactionFound
        transactionRepository.deleteById(transactionFounded.getId());

    }

    private void reverseAndUpdateBalanceAvailableAccountOriginAndDestinyByDeleteTx(Transaction transactionFounded) throws InsuficientFundsException {
        Account accountOrigin = transactionFounded.getPaymentMethod().getAccount();
        Account accountDestiny = transactionFounded.getPaymentMethodDestiny().getAccount();

        double amountOriginAccount = accountOrigin.getCurrentBalance();
        double amountDestinyAccount = accountDestiny.getCurrentBalance();

        if (amountDestinyAccount < transactionFounded.getAmount()) {
            throw new InsuficientFundsException("Saldo no disponible en la cuenta destino para reversar la operación antes de eliminarla, aumente el saldo en la cuenta origen para proceder");
        }

        amountOriginAccount = amountOriginAccount + transactionFounded.getAmount();
        amountDestinyAccount = amountDestinyAccount - transactionFounded.getAmount();


        //update balances account
        accountOrigin.setCurrentBalance(amountOriginAccount);
        accountDestiny.setCurrentBalance(amountDestinyAccount);

        accountRepository.save(accountOrigin);
        accountRepository.save(accountDestiny);
    }

    private void reverseAndUpDateBalanceAvailableAccountByDeleteTx(Transaction transactionFounded) throws InsuficientFundsException {
        //Setting a new available amount in the account
        Long idAccount = transactionFounded.getPaymentMethod().getAccount().getId();
        Account account = accountRepository.getReferenceById(idAccount);
        double newBalance = getNewBalanceAccountReverseByDeleteTx(transactionFounded, account);
        account.setCurrentBalance(newBalance);

        //update balance account
        accountRepository.save(account);
    }

    private Transaction updateLoanAssocReverseByDeleteTx(Transaction transactionRequest) {
        Long idTransactionLoanAssoc = transactionRequest.getIdLoanAssoc();
        Transaction transactionLoanAssoc = transactionRepository.findByIdAndUserId(idTransactionLoanAssoc, transactionRequest.getUserId());
        double currentRemainingLoanAssoc = transactionLoanAssoc.getRemaining();
        double newRemainingLoanAssoc = currentRemainingLoanAssoc + transactionRequest.getAmount();

        //update status if remaining was zero
        if(StatusEnum.PAYED.equals(transactionLoanAssoc.getStatus())) {
            transactionLoanAssoc.setStatus(StatusEnum.PENDING);
        }

        //update remaining loan assoc
        transactionLoanAssoc.setRemaining(newRemainingLoanAssoc);

        return transactionLoanAssoc;
    }

    private static double getNewBalanceAccountReverseByDeleteTx(Transaction transactionFounded, Account account) throws InsuficientFundsException {
        double currentBalance =  account.getCurrentBalance();
        double newBalance = 0.0;

        if(BlockEnum.IN.equals(transactionFounded.getBlock())) {
            newBalance = currentBalance - transactionFounded.getAmount();
        }

        if(BlockEnum.OUT.equals(transactionFounded.getBlock())) {
            newBalance = currentBalance + transactionFounded.getAmount();
        }

        if(newBalance < 0) {
            throw new InsuficientFundsException("Saldo insuficiente en la transacción asociada al registro actual");
        }

        return newBalance;
    }
}
