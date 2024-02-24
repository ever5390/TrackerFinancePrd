package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.dto.MovementDto;
import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.specification.SearcherTransactionSpecification;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionFiltersService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionFiltersServiceImpl implements ITransactionFiltersService {

    private final TransactionRepository transactionRepository;

    public static final Logger LOGGER = LoggerFactory.getLogger(TransactionFiltersServiceImpl.class);


    public TransactionFiltersServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    private LocalDateTime getParseLocalDateIfValid(String dateTime) throws Exception {

        try {
            if(StringUtils.isNotBlank(dateTime)) {
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("La fecha elegida no ha sido procesada correctamente por favor intente nuevamente");
        }

        return null;
    }

    @Override
    public ResumeMovementDto findMovementsByFilters(Long WorkspaceIdParam, String startDate, String endDate, TypeEnum type, StatusEnum status, String category, String description, String segment, String account, String paymentMethod, BlockEnum block, ActionEnum action) throws Exception {

        ResumeMovementDto resumeMovementDto = new ResumeMovementDto();

        List<MovementDto> movementsDto = new ArrayList<>();

        LocalDateTime startDateD = getParseLocalDateIfValid(startDate);
        LocalDateTime endDateD = getParseLocalDateIfValid(endDate);

        startDateD = startDateD==null?LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN):startDateD;
        endDateD = endDateD==null?LocalDateTime.now():endDateD;

        LOGGER.info("::: startDateD: "+startDateD);
        LOGGER.info("::: endDateD: "+endDateD);

        SearcherTransactionSpecification specification = new SearcherTransactionSpecification(WorkspaceIdParam, startDateD,
                endDateD, type, status, category, description, segment, account, paymentMethod, block, action);

        List<Transaction> transactions = transactionRepository.findAll(specification);

        double totalIN = 0.0;
        double totalOUT = 0.0;

        for (int i = 0; i < transactions.size(); i++) {
            MovementDto movementDto = new MovementDto();
            String descriptionToShow = transactions.get(i).getDescription();
            String reason = "";
            BlockEnum blockIN_OUT = transactions.get(i).getBlock();
            double amountMov = transactions.get(i).getAmount();
            String amountMovSave = amountMov + "";

            if(!TypeEnum.EXPENSE.equals(transactions.get(i).getType()) && !TypeEnum.INCOME.equals(transactions.get(i).getType())) {
                reason =  transactions.get(i).getDescription();
            }

            if(blockIN_OUT.equals(BlockEnum.IN)){
                totalIN+= amountMov;
                amountMovSave = "+"+ amountMovSave;
            }

            if(blockIN_OUT.equals(BlockEnum.OUT)){
                totalOUT+= amountMov;
                amountMovSave = "-"+ amountMovSave;
            }

            if(TypeEnum.LOAN.equals(transactions.get(i).getType()) || TypeEnum.PAYMENT.equals(transactions.get(i).getType())){
                descriptionToShow = transactions.get(i).getAction().toString().toUpperCase() + " ";

                if(transactions.get(i).getType().equals(TypeEnum.LOAN)) descriptionToShow+="PRÉSTAMO";
                if(transactions.get(i).getType().equals(TypeEnum.PAYMENT)) descriptionToShow+="PAGO";

                if(transactions.get(i).getAction().equals(ActionEnum.RECIBÍ)) descriptionToShow+=" de " + transactions.get(i).getCounterpart().getName();
                if(transactions.get(i).getAction().equals(ActionEnum.REALICÉ)) descriptionToShow+=" a " + transactions.get(i).getCounterpart().getName();
            }

            if(transactions.get(i).getType().equals(TypeEnum.TRANSFERENCE)) {

                descriptionToShow = transactions.get(i).getType().toString().toUpperCase() + " ( Desde " +  transactions.get(i).getPaymentMethod().getName() +
                        " hacia " + transactions.get(i).getPaymentMethodDestiny().getName()  + ")";

                if(account != null){

                    if(account.toUpperCase().equals(transactions.get(i).getPaymentMethod().getAccount().getName().toUpperCase())){
                        totalOUT+= amountMov;
                        amountMovSave = "-"+ amountMovSave;
                        descriptionToShow= transactions.get(i).getType().toString().toUpperCase();

                        if(transactions.get(i).getPaymentMethodDestiny().getAccount().getName().equals("EFECTIVO")) {
                            descriptionToShow = "RETIRO DE EFECTIVO";
                        }

                        if(account.equals("EFECTIVO")) {
                            descriptionToShow = "DEPÓSITO DE EFECTIVO";
                        }
                        descriptionToShow+= " ( Hacia " + transactions.get(i).getPaymentMethodDestiny().getAccount().getName()  + ")";
                    }

                    if(account.toUpperCase().equals(transactions.get(i).getPaymentMethodDestiny().getAccount().getName().toUpperCase())){
                        totalIN+= amountMov;
                        amountMovSave = "+"+ amountMovSave;
                        descriptionToShow= transactions.get(i).getType().toString().toUpperCase();
                        if(transactions.get(i).getPaymentMethod().getAccount().getName().equals("EFECTIVO")) {
                            descriptionToShow = "DEPÓSITO DE EFECTIVO";
                        }

                        if(account.equals("EFECTIVO")) {
                            descriptionToShow = "RETIRO DE EFECTIVO";
                        }

                        descriptionToShow+=" ( Desde " +  transactions.get(i).getPaymentMethod().getAccount().getName() + ")";
                    }

                }
                LOGGER.info("::::: account is null && pm != null ? :" + (account == null && paymentMethod != null));
                if(account == null && paymentMethod != null){
                    LOGGER.info("::::: entró :::");
                    if(paymentMethod.toUpperCase().equals(transactions.get(i).getPaymentMethod().getName().toUpperCase())){
                        totalOUT+= amountMov;
                        amountMovSave = "-"+ amountMovSave;
                        descriptionToShow= transactions.get(i).getType().toString().toUpperCase();

                        if(transactions.get(i).getPaymentMethodDestiny().getAccount().getName().equals("EFECTIVO")) {
                            descriptionToShow = "RETIRO DE EFECTIVO";
                        }

                        if(account.equals("EFECTIVO")) {
                            descriptionToShow = "DEPÓSITO DE EFECTIVO";
                        }
                        descriptionToShow+= " ( Hacia " + transactions.get(i).getPaymentMethodDestiny().getName()  + ")";
                    }

                    if(paymentMethod.toUpperCase().equals(transactions.get(i).getPaymentMethodDestiny().getName().toUpperCase())){
                        totalIN+= amountMov;
                        amountMovSave = "+"+ amountMovSave;
                        descriptionToShow= transactions.get(i).getType().toString().toUpperCase();
                        if(transactions.get(i).getPaymentMethod().getAccount().getName().equals("EFECTIVO")) {
                            descriptionToShow = "DEPÓSITO DE EFECTIVO";
                        }

                        if(account.equals("EFECTIVO")) {
                            descriptionToShow = "RETIRO DE EFECTIVO";
                        }
                        descriptionToShow+=" ( Desde " +  transactions.get(i).getPaymentMethod().getName() + ")";
                    }

                }

            }


            movementDto.setAmount(amountMovSave);
            movementDto.setHeaderTitle(descriptionToShow);
            movementDto.setDescription(reason.toLowerCase());
            movementDto.setStatus(transactions.get(i).getStatus().toString());
            movementDto.setCreateAt(transactions.get(i).getCreateAt());
            movementDto.setType(transactions.get(i).getType());
            movementDto.setAction(transactions.get(i).getAction());
            movementDto.setCategory(transactions.get(i).getSubCategory() != null?transactions.get(i).getSubCategory().getName():"");
            movementDto.setPaymentMethod(transactions.get(i).getPaymentMethod() != null ? transactions.get(i).getPaymentMethod().getName(): "");
            movementDto.setIdTransactionAssoc(transactions.get(i).getIdLoanAssoc());
            movementsDto.add(movementDto);
        }

        resumeMovementDto.setTotalNumberElements(transactions.size());
        resumeMovementDto.setTotalIN(totalIN);
        resumeMovementDto.setTotalOUT(totalOUT);
        resumeMovementDto.setMovememts(movementsDto);

        return resumeMovementDto;
    }

}
