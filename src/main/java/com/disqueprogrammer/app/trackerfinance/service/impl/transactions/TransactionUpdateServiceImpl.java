package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.PeriodEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionUpdateService;
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
import java.util.UUID;

@Transactional
@Service
public class TransactionUpdateServiceImpl implements ITransactionUpdateService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionUpdateServiceImpl.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final RecurringRepository recurringRepository;

    public TransactionUpdateServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, RecurringRepository recurringRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.recurringRepository = recurringRepository;
    }

    @Override
    public Transaction update(Transaction transactionRequest, Long idTransaction) throws InsuficientFundsException, CustomException {

            validateFormatAndCorrectValueAmount(transactionRequest);
            Transaction transactionFounded = transactionRepository.findByIdAndWorkspaceId(idTransaction, transactionRequest.getWorkspaceId());
            if (transactionFounded == null) throw new CustomException("No se encontró la transacción seleccionada");

            if (!TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
                reverseAndUpdateBalanceAvailableByUpdate(transactionRequest, transactionFounded);
            }

            if(TypeEnum.TRANSFERENCE.equals(transactionFounded.getType())) {
                reverseAndUpdateBalanceAvailableAccountsOriginAndDestinyByUpdateTx(transactionRequest, transactionFounded);
            }

            if (TypeEnum.LOAN.equals(transactionFounded.getType())
                    || (TypeEnum.EXPENSE.equals(transactionRequest.getType()) && transactionRequest.getAccount().getCardType().isFixedParameter())
                    || (TypeEnum.TRANSFERENCE.equals(transactionRequest.getType()) && transactionRequest.getAccount().getCardType().isFixedParameter()))  {
                //Update remaining to LOAN
                BigDecimal newRemainingLoanAssoc = getNewRemainingByEditLoan(transactionRequest, transactionFounded);
                if (newRemainingLoanAssoc.compareTo(BigDecimal.ZERO) == 0) transactionFounded.setStatus(StatusEnum.PAYED);
                transactionFounded.setRemaining(newRemainingLoanAssoc);
            }

            if (TypeEnum.PAYMENT.equals(transactionFounded.getType())) {
                //get Loan associated
                Long idTransactionLoanAssoc = transactionFounded.getTransactionLoanAssocToPay().getId();
                if (idTransactionLoanAssoc == null) throw new CustomException("Debe asociar un prèstamo al que haga referencia el pago a registrado.");
                Transaction transactionLoanAssoc = transactionRepository.findById(idTransactionLoanAssoc).orElseThrow(()-> new CustomException("El prèstamo al que hace referencia el pago registrado no existe."));

                //update remaining to loan associated
                BigDecimal newRemainingLoanAssoc = getNewRemainingLoanAssocByEditPayment(transactionRequest, transactionLoanAssoc, transactionFounded);

                if (newRemainingLoanAssoc.compareTo(BigDecimal.ZERO) == 0) {
                    transactionLoanAssoc.setStatus(StatusEnum.PAYED);
                }

                transactionLoanAssoc.setRemaining(newRemainingLoanAssoc);
                transactionRepository.save(transactionLoanAssoc);
            }
            
            //Only these data are updated
            transactionFounded.setAmount(transactionRequest.getAmount());
            transactionFounded.setCreateAt(transactionRequest.getCreateAt());
            transactionFounded.setDescription(transactionRequest.getDescription());
            transactionFounded.setRecurring(transactionRequest.getRecurring());
            transactionFounded.setTags(transactionRequest.getTags());
            transactionFounded.setSubCategory(transactionRequest.getSubCategory());
            transactionFounded.setCounterpart(transactionRequest.getCounterpart());
            transactionFounded.setPaymentMethod(transactionRequest.getPaymentMethod());

            return transactionRepository.save(transactionFounded);

    }

    private void validateFormatAndCorrectValueAmount( Transaction transactionReq) throws CustomException {

        int comparisonResult = transactionReq.getAmount().compareTo(BigDecimal.ZERO);

        if (comparisonResult <= 0)  throw new CustomException("El monto de la operación debe ser mayor a cero.");
    }

    private void reverseAndUpdateBalanceAvailableByUpdate(Transaction transactionRequest, Transaction transactionFounded) throws InsuficientFundsException {
        //Setting a new available amount in the account
        Long idAccount = transactionFounded.getAccount().getId();
        Account accountCurrent = accountRepository.findById(idAccount).get();
        BigDecimal newBalance = getNewBalanceAccountUpdate(transactionFounded, transactionRequest, accountCurrent);
        accountCurrent.setCurrentBalance(newBalance.setScale(2, RoundingMode.HALF_UP));

        //update balance account
        accountRepository.save(accountCurrent);
    }

    private void reverseAndUpdateBalanceAvailableAccountsOriginAndDestinyByUpdateTx(Transaction transactionRequest, Transaction transactionFounded) throws InsuficientFundsException {
        Long idAccountOrigin = transactionFounded.getAccount().getId();
        Long idAccountDestiny = transactionFounded.getAccount().getId();
        //Al estar registrados ya se aseguran que las cuentas existan y pertenezcan al usuario.
        Account accountOrigin = accountRepository.findById(idAccountOrigin).get();
        Account accountDestiny = accountRepository.findById(idAccountDestiny).get();

        BigDecimal amountOriginAccount = accountOrigin.getCurrentBalance();
        BigDecimal amountDestinyAccount = accountDestiny.getCurrentBalance();
        BigDecimal amountCurrent = transactionFounded.getAmount();
        BigDecimal absDifferenceCurrentAmountAndNewAmount = (amountCurrent.subtract(transactionRequest.getAmount()).abs());

        if (amountCurrent.compareTo(transactionRequest.getAmount()) < 0) {
            amountOriginAccount = amountOriginAccount.subtract(absDifferenceCurrentAmountAndNewAmount);
            amountDestinyAccount = amountDestinyAccount.add(absDifferenceCurrentAmountAndNewAmount);

            if (amountOriginAccount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsuficientFundsException("Saldo insuficiente en la cuenta origen para este nuevo monto en la operación");
            }

        } else {
            amountOriginAccount = amountOriginAccount.add(absDifferenceCurrentAmountAndNewAmount);
            amountDestinyAccount = amountDestinyAccount.subtract(absDifferenceCurrentAmountAndNewAmount);

            if (amountDestinyAccount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsuficientFundsException("Saldo insuficiente en la cuenta destino para este nuevo monto en la operación");
            }
        }

        //Setter new balances
        accountOrigin.setCurrentBalance(amountOriginAccount.setScale(2, RoundingMode.HALF_UP));
        accountDestiny.setCurrentBalance(amountDestinyAccount.setScale(2, RoundingMode.HALF_UP));

        accountRepository.save(accountOrigin);
        accountRepository.save(accountDestiny);
    }

    private static BigDecimal getNewRemainingByEditLoan(Transaction transactionRequest, Transaction transactionFound) throws CustomException {
        BigDecimal newRemainingLoan = BigDecimal.ZERO;
        BigDecimal currentRemainingLoanAssoc = transactionFound.getRemaining();
        BigDecimal absDifferenceCurrentAmountAndNewAmount = (transactionFound.getAmount().subtract(transactionRequest.getAmount())).abs();

        if(transactionRequest.getAmount().compareTo(transactionFound.getAmount()) > 0) {
            newRemainingLoan = currentRemainingLoanAssoc.add(absDifferenceCurrentAmountAndNewAmount);
        } else {
            newRemainingLoan = currentRemainingLoanAssoc.subtract(absDifferenceCurrentAmountAndNewAmount);
        }

        if (newRemainingLoan.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("El nuevo monto a actualizar termina siendo mayor al monto pendiente de pago del préstamos asociado por (S./" +  newRemainingLoan + ")");
        }
        return newRemainingLoan;
    }

    private static BigDecimal getNewRemainingLoanAssocByEditPayment(Transaction transactionRequest, Transaction transactionLoanAssoc, Transaction transactionFound) throws CustomException {
        BigDecimal newRemainingLoanAssoc = BigDecimal.ZERO;
        BigDecimal currentRemainingLoanAssoc = transactionLoanAssoc.getRemaining();
        BigDecimal absDifferenceCurrentAmountAndNewAmount = (transactionFound.getAmount().subtract(transactionRequest.getAmount())).abs();

        if(transactionFound.getAmount().compareTo(transactionRequest.getAmount()) < 0) {
            newRemainingLoanAssoc = currentRemainingLoanAssoc.add(absDifferenceCurrentAmountAndNewAmount);
        } else {
            newRemainingLoanAssoc = currentRemainingLoanAssoc.subtract(absDifferenceCurrentAmountAndNewAmount);
        }

        if (newRemainingLoanAssoc.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("El nuevo monto a actualizar termina siendo mayor al monto pendiente de pago del préstamos asociado por (S./" +  newRemainingLoanAssoc + ")");
        }
        return newRemainingLoanAssoc;
    }

    private static BigDecimal getNewBalanceAccountUpdate(Transaction transactionCurrent, Transaction transactionRequest, Account account) throws InsuficientFundsException {

        BigDecimal newBalance = BigDecimal.ZERO;
        BigDecimal currentBalance =  account.getCurrentBalance();
        BigDecimal absDifferenceCurrentAmountAndNewAmount = (transactionCurrent.getAmount().subtract(transactionRequest.getAmount()).abs());

        int comparisonResult = transactionCurrent.getAmount().compareTo(transactionRequest.getAmount());

        if(BlockEnum.IN.equals(transactionCurrent.getBlock())) {
            if(comparisonResult < 0) {
                newBalance = currentBalance.add(absDifferenceCurrentAmountAndNewAmount);
            } else {
                newBalance = currentBalance.subtract(absDifferenceCurrentAmountAndNewAmount);
            }

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsuficientFundsException("Saldo insuficiente para efectuar esta transacción");
            }
        }

        if(BlockEnum.OUT.equals(transactionRequest.getBlock())) {
            if(comparisonResult < 0) {
                newBalance = currentBalance.subtract(absDifferenceCurrentAmountAndNewAmount);
            } else {
                newBalance = currentBalance.add(absDifferenceCurrentAmountAndNewAmount);
            }
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsuficientFundsException("Saldo insuficiente para efectuar esta transacción");
            }
        }

        return newBalance;
    }

    @Override
    public Transaction updateTransactionRecurring(Transaction transactionRecurringToUpdate, Long transactionRecurringId) throws Exception {
        try {
            Recurring recurring = transactionRecurringToUpdate.getRecurring();
            return updateTransactionRecurringByDateCreatedTx(transactionRecurringToUpdate, recurring, transactionRecurringToUpdate.getCreateAt());
        } catch (Exception error) {
            LOG.error(error.getLocalizedMessage());
            throw new Exception("Ocurrió un error al procesar la operación a actualizar.");
        }

    }

    private Transaction updateTransactionRecurringByDateCreatedTx(Transaction nextTransactionRecurring, Recurring recurring, LocalDateTime localDateTimeReq) {
        Transaction transactionToUpdate = new Transaction();
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

            transactionToUpdate = (setterNewTransaction(nextTransactionRecurring, this.recurringRepository.save(newRecurring)));

        }

        return transactionRepository.save(transactionToUpdate);
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

    private static LocalDateTime getDateTimeRegenerationRecurringPerPeriodYear(Transaction transactionRequest, Recurring newRecurring, int numberOfTimes) {
        int lengthOfMonth = newRecurring.getNextClosestPaymentDate().plusYears(numberOfTimes).toLocalDate().lengthOfMonth();
        int yearPerPeriodYear = newRecurring.getNextClosestPaymentDate().plusYears(numberOfTimes).getYear();
        int monthPerPeriodYear = transactionRequest.getRecurring().getItemDateSelectedPerPeriod().get(0);
        LocalDate newNextPaymentDate = LocalDate.of(yearPerPeriodYear, monthPerPeriodYear, lengthOfMonth);
        LocalTime hour = LocalTime.of(12, 0);
        return LocalDateTime.of(newNextPaymentDate, hour);
    }

}
