package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.dto.MovementDto;
import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.*;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionGetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class TransactionGetServiceImpl implements ITransactionGetService {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionGetServiceImpl.class);

    private final TransactionRepository transactionRepository;

    public TransactionGetServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction findByIdAndWorkspaceId(Long transactionRequestId, Long workspaceId) throws CustomException {

        Transaction transaction = transactionRepository.findByIdAndWorkspaceId(transactionRequestId, workspaceId);
        if (transaction == null)  throw new CustomException("No se encontró la transacción seleccionada");
        return transaction;
    }

    public ResumeMovementDto findByWorkspaceId(Long workspaceId) throws Exception {

        ResumeMovementDto resumeMovementDto = new ResumeMovementDto();

        List<MovementDto> movementsDto = new ArrayList<>();

        List<Transaction> transactions = transactionRepository.findByWorkspaceId(workspaceId);

        BigDecimal totalIN = BigDecimal.ZERO;
        BigDecimal totalOUT = BigDecimal.ZERO;

        for (int i = 0; i < transactions.size(); i++) {

            MovementDto movementDto = new MovementDto();
            String descriptionToShow = transactions.get(i).getDescription();

            String reason = "";
            BlockEnum blockIN_OUT = transactions.get(i).getBlock();
            BigDecimal amountMov = transactions.get(i).getAmount();
            String amountMovSave = amountMov + "";

            if(!TypeEnum.EXPENSE.equals(transactions.get(i).getType()) && !TypeEnum.INCOME.equals(transactions.get(i).getType())) {
                reason =  transactions.get(i).getDescription();
            }

            if(blockIN_OUT.equals(BlockEnum.IN)){
                totalIN = totalIN.add(amountMov);
                amountMovSave = "+"+ amountMovSave;
            }

            if(blockIN_OUT.equals(BlockEnum.OUT)){
                totalOUT = totalOUT.add(amountMov);
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

                descriptionToShow = transactions.get(i).getType().toString().toUpperCase() + " ( Desde " +  transactions.get(i).getAccount().getName() +
                        " hacia " + transactions.get(i).getAccountDestiny().getName()  + ")";

                if(transactions.get(i).getAccountDestiny().getName().equals("EFECTIVO")) {
                    descriptionToShow = "RETIRO DE EFECTIVO: ( Desde cuenta " + transactions.get(i).getAccount().getName() + ")";
                }

                if(transactions.get(i).getAccount().getName().equals("EFECTIVO")) {
                    descriptionToShow = "DEPÓSITO DE EFECTIVO: ( Hacia cuenta " + transactions.get(i).getAccount().getName() + ")";
                }

            }

            movementDto.setAmount(amountMovSave);
            movementDto.setHeaderTitle(descriptionToShow);
            movementDto.setDescription(reason.toLowerCase());
            movementDto.setStatus(transactions.get(i).getStatus().toString());
            movementDto.setAccount(transactions.get(i).getAccount().getName());
            movementDto.setSubCategory(transactions.get(i).getSubCategory().getName());
            movementDto.setPaymentMethod(transactions.get(i).getPaymentMethod().getName());
            movementDto.setCreateAt(transactions.get(i).getCreateAt());
            movementDto.setType(transactions.get(i).getType());
            movementsDto.add(movementDto);
        }

        resumeMovementDto.setTotalIN(totalIN);
        resumeMovementDto.setTotalOUT(totalOUT);
        resumeMovementDto.setMovememts(transactions);

        return resumeMovementDto;
    }

    @Override
    public List<Transaction> findByStatusAndWorkspaceId(StatusEnum status, Long workspaceId) {
        return this.transactionRepository.findByStatusAndWorkspaceId(status, workspaceId);
    }

    @Override
    public List<Transaction> findAllTxByWorkspaceId(Long workspaceId) {
        return this.transactionRepository.findByWorkspaceId(workspaceId);
    }


}
