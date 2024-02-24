package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedCounterpartException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.*;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionSaveService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
    public Transaction save(Transaction transactionRequest) throws CustomException, AccountEqualsException, InsuficientFundsException, UnspecifiedCounterpartException {

        settingDefaultParameters(transactionRequest);
        validationAmount(transactionRequest.getAmount());
        validCreateAt(transactionRequest.getCreateAt());
        transactionRequest.setSubCategory((validateCategory(transactionRequest)));
        transactionRequest.setPaymentMethod(validatePaymentMethod(transactionRequest.getPaymentMethod(), transactionRequest.getType(), "origen", transactionRequest.getWorkspaceId()));
        transactionRequest.setPaymentMethodDestiny(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())?null:validatePaymentMethod(transactionRequest.getPaymentMethodDestiny(), transactionRequest.getType(), "destino", transactionRequest.getWorkspaceId()));
        validateAccountBalanceAvailableForEnteredAmount(transactionRequest, transactionRequest.getPaymentMethod().getAccount());

        if (TypeEnum.LOAN.equals(transactionRequest.getType())) {
            //Valid counterparty counterpart
            transactionRequest.setCounterpart(validCounterpartForLoanTransaction(transactionRequest));
        }

        if (TypeEnum.PAYMENT.equals(transactionRequest.getType())) {
            //update loan assoc transaction in DB
            Transaction transactionLoanAssocUpdated = updateLoanAssocFromPaymentToSave(transactionRequest);
            transactionRepository.save(transactionLoanAssocUpdated);

            //Update Counterpart to payment request
            transactionRequest.setCounterpart(transactionLoanAssocUpdated.getCounterpart());
        }

        if (!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType())) {
            //proccessing balance account
            Long idAccount = transactionRequest.getPaymentMethod().getAccount().getId();
            Account account = accountRepository.findByIdAndWorkspaceId(idAccount, transactionRequest.getWorkspaceId());
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

        // Case recurring
        if(transactionRequest.getRecurring() != null) {
            transactionRequest.getRecurring().setStatusIsPayed(true);
            generateNextTransactionWithRecurring(transactionRequest);
        }

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

    private void validationAmount(double amount) throws CustomException {
        Double.parseDouble(String.valueOf(amount));
        if (amount <= 0)  throw new CustomException("El monto de la operación debe ser mayor a cero.");
    }

    private PaymentMethod validatePaymentMethod(PaymentMethod paymentMethodReq, TypeEnum typeOperation, String typePaymentMethod, Long workspaceId) throws CustomException {

        String originOrDestiny = TypeEnum.TRANSFERENCE.equals(typeOperation)?typePaymentMethod:"";

        if(paymentMethodReq == null || StringUtils.isEmpty(paymentMethodReq.getId().toString())) {
            throw new CustomException("El método de pago " + originOrDestiny + " no ha sido encontrado");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndWorkspaceId(paymentMethodReq.getId(), workspaceId);
        if(paymentMethod == null) {
            throw new CustomException("El método de pago " + originOrDestiny  + " no ha sido encontrado");
        }

        return paymentMethod;
    }

    private SubCategory validateCategory(Transaction transactionRequest) throws CustomException {

        if(!TypeEnum.TRANSFERENCE.equals(transactionRequest.getType()) && transactionRequest.getSubCategory() != null && transactionRequest.getSubCategory().getId() != null && transactionRequest.getSubCategory().getId() != 0 ) {
            SubCategory subCategory = subCategoryRepository.findByIdAndWorkspaceId(transactionRequest.getSubCategory().getId(), transactionRequest.getWorkspaceId());
            if(subCategory == null) {
                throw new CustomException("La categoría no ha sido encontrado");
            }
            return subCategory;
        }

        return null;
    }

    private void settingDefaultParameters(Transaction transactionRequest) throws CustomException {

        if(transactionRequest.getType() == null) throw new CustomException("Por favor seleccione el tipo de operación para poder procesar la operación");

        if (transactionRequest.getType().equals(TypeEnum.TRANSFERENCE)) {
            transactionRequest.setBlock(BlockEnum.NOT_APPLICABLE);
            transactionRequest.setAction(ActionEnum.NOT_APPLICABLE);
            transactionRequest.setRemaining(0);
            transactionRequest.setCounterpart(null);
            transactionRequest.setSubCategory(null);
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
            transactionRequest.setCounterpart(null);
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

    }

    private Counterpart validCounterpartForLoanTransaction(Transaction transactionRequest) throws UnspecifiedCounterpartException {

        String message = transactionRequest.getAction().equals(ActionEnum.RECIBÍ)?" de quien se recibió ": "a quien se otorgó ";
        if (transactionRequest.getCounterpart() == null ) throw new UnspecifiedCounterpartException("Ocurrió un error al intentar detectar a la persona " + message + "el préstamo.");
        Counterpart counterParty =  counterpartRepository.findByIdAndWorkspaceId(transactionRequest.getCounterpart().getId(), transactionRequest.getWorkspaceId());
        if(counterParty == null) throw new UnspecifiedCounterpartException("Ocurrió un error al intentar detectar a la persona " + message + "el préstamo.");
        return counterParty;
    }

    private Transaction updateLoanAssocFromPaymentToSave(Transaction transactionRequest) throws CustomException {

            Long idTransactionLoanAssoc = transactionRequest.getIdLoanAssoc();
            if (idTransactionLoanAssoc == null) throw new CustomException("El préstamo al que hace referencia el pago registrado no existe.");
            Transaction transactionLoanAssoc = transactionRepository.findByIdAndWorkspaceId(idTransactionLoanAssoc, transactionRequest.getWorkspaceId());
            if(transactionLoanAssoc == null) new CustomException("El préstamo al que hace referencia el pago registrado no existe.");

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
        nextTransactionRecurring.setIdLoanAssoc(transactionReq.getIdLoanAssoc());
        nextTransactionRecurring.setPaymentMethod(transactionReq.getPaymentMethod());
        nextTransactionRecurring.setPaymentMethodDestiny(transactionReq.getPaymentMethodDestiny());
        nextTransactionRecurring.setCounterpart(transactionReq.getCounterpart());
        nextTransactionRecurring.setUserId(transactionReq.getUserId());
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
