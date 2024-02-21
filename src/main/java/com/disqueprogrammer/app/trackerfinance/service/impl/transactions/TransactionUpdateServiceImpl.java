package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class TransactionUpdateServiceImpl implements ITransactionUpdateService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionUpdateServiceImpl.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    public TransactionUpdateServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction update(Transaction transactionRequest, Long idTransaction) throws ObjectNotFoundException, InsuficientFundsException, CustomException {

            validateFormatAndCorrectValueAmount(transactionRequest.getAmount());
            Transaction transactionFounded = transactionRepository.findByIdAndUserId(idTransaction, transactionRequest.getUserId());
            if (transactionFounded == null) throw new ObjectNotFoundException("No se encontró la transacción seleccionada");

            if (!TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
                reverseAndUpdateBalanceAvailableByUpdate(transactionRequest, transactionFounded);
                //Update params only this case
                transactionFounded.setCategory(transactionRequest.getCategory());
                transactionFounded.setSegment(transactionRequest.getSegment());
            }

            if(TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
                reverseAndUpdateBalanceAvailableAccountsOriginAndDestinyByUpdateTx(transactionRequest, transactionFounded);
            }

            if (TypeEnum.LOAN.equals(transactionFounded.getType())) {
                //Update remaining to LOAN
                double newRemainingLoanAssoc = getNewRemainingByEditLoan(transactionRequest, transactionFounded);
                if (newRemainingLoanAssoc == 0) transactionFounded.setStatus(StatusEnum.PAYED);
                transactionFounded.setRemaining(newRemainingLoanAssoc);
            }

            if (TypeEnum.PAYMENT.equals(transactionFounded.getType())) {
                //get Loan associated
                Long idTransactionLoanAssoc = transactionFounded.getIdLoanAssoc();
                if (idTransactionLoanAssoc == null) throw new CustomException("Debe asociar un prèstamo al que haga referencia el pago a registrado.");
                Transaction transactionLoanAssoc = transactionRepository.findById(idTransactionLoanAssoc).orElseThrow(()-> new CustomException("El prèstamo al que hace referencia el pago registrado no existe."));

                //update remaining to loan associated
                double newRemainingLoanAssoc = getNewRemainingLoanAssocByEditPayment(transactionRequest, transactionLoanAssoc, transactionFounded);

                if (newRemainingLoanAssoc == 0) {
                    transactionLoanAssoc.setStatus(StatusEnum.PAYED);
                }

                transactionLoanAssoc.setRemaining(newRemainingLoanAssoc);
                transactionRepository.save(transactionLoanAssoc);
            }
            
            //Only these data are updated
            transactionFounded.setAmount(transactionRequest.getAmount());
            transactionFounded.setCreateAt(transactionRequest.getCreateAt());
            transactionFounded.setDescription(transactionRequest.getDescription());

            return transactionRepository.save(transactionFounded);

    }

    private void validateFormatAndCorrectValueAmount(double amount) throws CustomException {
        try {
            Double.parseDouble(String.valueOf(amount));
            if (amount <= 0)  throw new CustomException("El monto de la operación debe ser mayor a cero.");
        } catch (CustomException excepcion) {
            throw new CustomException("Hubo un problema al intentar leer el monto de la operación a realizar");
        }
    }

    private void reverseAndUpdateBalanceAvailableByUpdate(Transaction transactionRequest, Transaction transactionFounded) throws InsuficientFundsException {
        //Setting a new available amount in the account
        Long idAccount = transactionFounded.getPaymentMethod().getAccount().getId();
        Account accountCurrent = accountRepository.findById(idAccount).get();
        double newBalance = getNewBalanceAccountUpdate(transactionFounded, transactionRequest, accountCurrent);
        accountCurrent.setCurrentBalance(newBalance);

        //update balance account
        accountRepository.save(accountCurrent);
    }

    private void reverseAndUpdateBalanceAvailableAccountsOriginAndDestinyByUpdateTx(Transaction transactionRequest, Transaction transactionFounded) throws InsuficientFundsException {
        Long idAccountOrigin = transactionFounded.getPaymentMethod().getAccount().getId();
        Long idAccountDestiny = transactionFounded.getPaymentMethodDestiny().getAccount().getId();
        //Al estar registrados ya se aseguran que las cuentas existan y pertenezcan al usuario.
        Account accountOrigin = accountRepository.findById(idAccountOrigin).get();
        Account accountDestiny = accountRepository.findById(idAccountDestiny).get();

        double amountOriginAccount = accountOrigin.getCurrentBalance();
        double amountDestinyAccount = accountDestiny.getCurrentBalance();
        double amountCurrent = transactionFounded.getAmount();
        double absDifferenceCurrentAmountAndNewAmount = Math.abs(amountCurrent - transactionRequest.getAmount());

        if (amountCurrent < transactionRequest.getAmount()) {
            amountOriginAccount = amountOriginAccount - absDifferenceCurrentAmountAndNewAmount;
            amountDestinyAccount = amountDestinyAccount + absDifferenceCurrentAmountAndNewAmount;

            if (amountOriginAccount < 0) {
                throw new InsuficientFundsException("Saldo insuficiente en la cuenta origen para este nuevo monto en la operación");
            }

        } else {
            amountOriginAccount = amountOriginAccount + absDifferenceCurrentAmountAndNewAmount;
            amountDestinyAccount = amountDestinyAccount - absDifferenceCurrentAmountAndNewAmount;

            if (amountDestinyAccount < 0) {
                throw new InsuficientFundsException("Saldo insuficiente en la cuenta destino para este nuevo monto en la operación");
            }
        }

        //Setter new balances
        accountOrigin.setCurrentBalance(amountOriginAccount);
        accountDestiny.setCurrentBalance(amountDestinyAccount);

        accountRepository.save(accountOrigin);
        accountRepository.save(accountDestiny);
    }

    private static double getNewRemainingByEditLoan(Transaction transactionRequest, Transaction transactionFound) throws CustomException {
        double newRemainingLoan = 0;
        double currentRemainingLoanAssoc = transactionFound.getRemaining();
        double absDifferenceCurrentAmountAndNewAmount = Math.abs(transactionFound.getAmount() - transactionRequest.getAmount());

        if(transactionRequest.getAmount() > transactionFound.getAmount()) {
            newRemainingLoan = currentRemainingLoanAssoc + absDifferenceCurrentAmountAndNewAmount;
        } else {
            newRemainingLoan = currentRemainingLoanAssoc - absDifferenceCurrentAmountAndNewAmount;
        }

        if (newRemainingLoan < 0) {
            throw new CustomException("El nuevo monto a actualizar termina siendo mayor al monto pendiente de pago del préstamos asociado por (S./" +  newRemainingLoan + ")");
        }
        return newRemainingLoan;
    }

    private static double getNewRemainingLoanAssocByEditPayment(Transaction transactionRequest, Transaction transactionLoanAssoc, Transaction transactionFound) throws CustomException {
        double newRemainingLoanAssoc = 0;
        double currentRemainingLoanAssoc = transactionLoanAssoc.getRemaining();
        double absDifferenceCurrentAmountAndNewAmount = Math.abs(transactionFound.getAmount() - transactionRequest.getAmount());

        if(transactionFound.getAmount() < transactionRequest.getAmount()) {
            newRemainingLoanAssoc = currentRemainingLoanAssoc + absDifferenceCurrentAmountAndNewAmount;
        } else {
            newRemainingLoanAssoc = currentRemainingLoanAssoc - absDifferenceCurrentAmountAndNewAmount;
        }

        if (newRemainingLoanAssoc < 0) {
            throw new CustomException("El nuevo monto a actualizar termina siendo mayor al monto pendiente de pago del préstamos asociado por (S./" +  newRemainingLoanAssoc + ")");
        }
        return newRemainingLoanAssoc;
    }

    private static double getNewBalanceAccountUpdate(Transaction transactionCurrent, Transaction transactionRequest, Account account) throws InsuficientFundsException {

        double newBalance = 0.0;
        double currentBalance =  account.getCurrentBalance();
        double absDifferenceCurrentAmountAndNewAmount = Math.abs(transactionCurrent.getAmount() - transactionRequest.getAmount());

        if(BlockEnum.IN.equals(transactionCurrent.getBlock())) {
            if(transactionCurrent.getAmount() < transactionRequest.getAmount()) {
                newBalance = currentBalance + absDifferenceCurrentAmountAndNewAmount;
            } else {
                newBalance = currentBalance - absDifferenceCurrentAmountAndNewAmount;
            }

            if (newBalance < 0) {
                throw new InsuficientFundsException("Saldo insuficiente para efectuar esta transacción");
            }
        }

        if(BlockEnum.OUT.equals(transactionRequest.getBlock())) {
            if(transactionCurrent.getAmount() < transactionRequest.getAmount()) {
                newBalance = currentBalance - absDifferenceCurrentAmountAndNewAmount;
            } else {
                newBalance = currentBalance + absDifferenceCurrentAmountAndNewAmount;
            }

            if (newBalance < 0) {
                throw new InsuficientFundsException("Saldo insuficiente para efectuar esta transacción");
            }
        }

        return newBalance;
    }

}
