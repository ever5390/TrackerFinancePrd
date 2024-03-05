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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public void delete(Long transactionRequestId, Long workspaceId) throws CustomException, InsuficientFundsException {

        //Exist Tx
        Transaction transactionFounded = transactionRepository.findByIdAndWorkspaceId(transactionRequestId, workspaceId);
        if (transactionFounded == null) throw new CustomException("No se encontró la transacción seleccionada");

        if (!TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
            reverseAndUpDateBalanceAvailableAccountByDeleteTx(transactionFounded);
        }

        if (TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
            reverseAndUpdateBalanceAvailableAccountOriginAndDestinyByDeleteTx(transactionFounded);
        }

        if (TypeEnum.LOAN.equals(transactionFounded.getType())
                || (TypeEnum.EXPENSE.equals(transactionFounded.getType()) && transactionFounded.getAccount().getCardType().isFixedParameter())
                || (TypeEnum.TRANSFERENCE.equals(transactionFounded.getType()) && transactionFounded.getAccount().getCardType().isFixedParameter())) {

            //Searching for transactions Payment type associated with this operation
            List<Transaction> paymentTransaction = transactionRepository.findPaymentsByLoanIdAssocAndWorkspaceId(transactionRequestId, transactionFounded.getWorkspaceId());
            if(!paymentTransaction.isEmpty())  throw new CustomException("No es posible eliminar la transacción ya que se encontraron pagos asociados a ella.");
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
        Account accountOrigin = transactionFounded.getAccount();
        Account accountDestiny = transactionFounded.getAccount();

        BigDecimal amountOriginAccount = accountOrigin.getCurrentBalance();
        BigDecimal amountDestinyAccount = accountDestiny.getCurrentBalance();

        if (amountDestinyAccount.compareTo(transactionFounded.getAmount()) < 0) {
            throw new InsuficientFundsException("Saldo no disponible en la cuenta destino para reversar la operación antes de eliminarla, aumente el saldo en la cuenta origen para proceder");
        }

        amountOriginAccount = amountOriginAccount.add(transactionFounded.getAmount());
        amountDestinyAccount = amountDestinyAccount.subtract(transactionFounded.getAmount());


        //update balances account
        accountOrigin.setCurrentBalance(amountOriginAccount.setScale(2, RoundingMode.HALF_UP));
        accountDestiny.setCurrentBalance(amountDestinyAccount.setScale(2, RoundingMode.HALF_UP));

        accountRepository.save(accountOrigin);
        accountRepository.save(accountDestiny);
    }

    private void reverseAndUpDateBalanceAvailableAccountByDeleteTx(Transaction transactionFounded) throws InsuficientFundsException {
        //Setting a new available amount in the account
        Long idAccount = transactionFounded.getAccount().getId();
        Account account = accountRepository.getReferenceById(idAccount);
        BigDecimal newBalance = getNewBalanceAccountReverseByDeleteTx(transactionFounded, account);
        account.setCurrentBalance(newBalance.setScale(2, RoundingMode.HALF_UP));

        //update balance account
        accountRepository.save(account);
    }

    private Transaction updateLoanAssocReverseByDeleteTx(Transaction transactionRequest) {
        Long idTransactionLoanAssoc = transactionRequest.getTransactionLoanAssocToPay().getId();
        Transaction transactionLoanAssoc = transactionRepository.findByIdAndWorkspaceId(idTransactionLoanAssoc, transactionRequest.getWorkspaceId());
        BigDecimal currentRemainingLoanAssoc = transactionLoanAssoc.getRemaining();
        BigDecimal newRemainingLoanAssoc = currentRemainingLoanAssoc.add(transactionRequest.getAmount());

        //update status if remaining was zero
        if(StatusEnum.PAYED.equals(transactionLoanAssoc.getStatus())) {
            transactionLoanAssoc.setStatus(StatusEnum.PENDING);
        }

        //update remaining loan assoc
        transactionLoanAssoc.setRemaining(newRemainingLoanAssoc);

        return transactionLoanAssoc;
    }

    private static BigDecimal getNewBalanceAccountReverseByDeleteTx(Transaction transactionFounded, Account account) throws InsuficientFundsException {
        BigDecimal currentBalance =  account.getCurrentBalance();
        BigDecimal newBalance = BigDecimal.ZERO;

        if(BlockEnum.IN.equals(transactionFounded.getBlock())) {
            newBalance = currentBalance.subtract(transactionFounded.getAmount());
        }

        if(BlockEnum.OUT.equals(transactionFounded.getBlock())) {
            newBalance = currentBalance.add(transactionFounded.getAmount());
        }

        if(newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsuficientFundsException("Saldo insuficiente en la transacción asociada al registro actual");
        }

        return newBalance;
    }
}
