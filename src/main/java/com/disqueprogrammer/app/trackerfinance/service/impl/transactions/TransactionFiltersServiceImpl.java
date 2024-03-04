package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

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
    public ResumeMovementDto findMovementsByFilters(Long WorkspaceIdParam, String startDate, String endDate, TypeEnum type, StatusEnum status, String subCategory, String description, String segment, String account, String paymentMethod, BlockEnum block, ActionEnum action, String responsableUser) throws Exception {

        ResumeMovementDto resumeMovementDto = new ResumeMovementDto();

        LocalDateTime startDateD = getParseLocalDateIfValid(startDate);
        LocalDateTime endDateD = getParseLocalDateIfValid(endDate);

        startDateD = startDateD==null?LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN):startDateD;
        endDateD = endDateD==null?LocalDateTime.now():endDateD;

        LOGGER.info("::: startDateD: "+startDateD);
        LOGGER.info("::: endDateD: "+endDateD);

        SearcherTransactionSpecification specification = new SearcherTransactionSpecification(WorkspaceIdParam, startDateD,
                endDateD, type, status, subCategory, description, account, paymentMethod, block, action, responsableUser);

        List<Transaction> transactions = transactionRepository.findAll(specification);

        double totalIN = 0.0;
        double totalOUT = 0.0;
        double totalTheyOweMe = 0.0;
        double totalIOweYou = 0.0;

        for (Transaction transaction : transactions) {
            BlockEnum blockIN_OUT = transaction.getBlock();
            double amountMov = transaction.getAmount();

            if (blockIN_OUT.equals(BlockEnum.IN)) totalIN += amountMov;
            if (blockIN_OUT.equals(BlockEnum.OUT)) totalOUT += amountMov;

            if(transaction.getType().equals(TypeEnum.LOAN) && transaction.getAction().equals(ActionEnum.REALICÉ)) {
                totalTheyOweMe += transaction.getRemaining();
            }

            if(transaction.getType().equals(TypeEnum.LOAN) && transaction.getAction().equals(ActionEnum.RECIBÍ)) {
                totalIOweYou += transaction.getRemaining();
            }

            if (transaction.getType().equals(TypeEnum.TRANSFERENCE) && account != null) {
                if (account.equalsIgnoreCase(transaction.getAccount().getName()))
                    totalOUT += amountMov;

                if (account.equalsIgnoreCase(transaction.getAccountDestiny().getName()))
                    totalIN += amountMov;
            }
        }

        resumeMovementDto.setTotalIOweYou(totalIOweYou);
        resumeMovementDto.setTotalTheyOweMe(totalTheyOweMe);
        resumeMovementDto.setTotalIN(totalIN);
        resumeMovementDto.setTotalOUT(totalOUT);
        resumeMovementDto.setMovememts(transactions);

        return resumeMovementDto;
    }

}
