package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.dto.MovementDto;
import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
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
import org.springframework.web.bind.annotation.PathVariable;

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
    public Transaction findByIdAndUserId(Long transactionRequestId, Long userId) throws ObjectNotFoundException {

        Transaction transaction = transactionRepository.findByIdAndUserId(transactionRequestId, userId);
        if (transaction == null)  throw new ObjectNotFoundException("No se encontró la transacción seleccionada");
        return transaction;
    }

    public ResumeMovementDto findByUserId(Long userId) throws Exception {

        ResumeMovementDto resumeMovementDto = new ResumeMovementDto();

        List<MovementDto> movementsDto = new ArrayList<>();

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

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

                if(transactions.get(i).getAction().equals(ActionEnum.RECIBÍ)) descriptionToShow+=" de " + transactions.get(i).getMember().getName();
                if(transactions.get(i).getAction().equals(ActionEnum.REALICÉ)) descriptionToShow+=" a " + transactions.get(i).getMember().getName();
            }

            if(transactions.get(i).getType().equals(TypeEnum.TRANSFERENCE)) {

                descriptionToShow = transactions.get(i).getType().toString().toUpperCase() + " ( Desde " +  transactions.get(i).getPaymentMethod().getName() +
                        " hacia " + transactions.get(i).getPaymentMethodDestiny().getName()  + ")";

                if(transactions.get(i).getPaymentMethodDestiny().getName().equals("EFECTIVO")) {
                    descriptionToShow = "RETIRO DE EFECTIVO: ( Desde cuenta " + transactions.get(i).getPaymentMethod().getAccount().getName() + ")";
                }

                if(transactions.get(i).getPaymentMethod().getName().equals("EFECTIVO")) {
                    descriptionToShow = "DEPÓSITO DE EFECTIVO: ( Hacia cuenta " + transactions.get(i).getPaymentMethodDestiny().getAccount().getName() + ")";
                }

            }

            movementDto.setAmount(amountMovSave);
            movementDto.setHeaderTitle(descriptionToShow);
            movementDto.setDescription(reason.toLowerCase());
            movementDto.setStatus(transactions.get(i).getStatus().toString());
            movementDto.setCreateAt(transactions.get(i).getCreateAt());
            movementDto.setType(transactions.get(i).getType());
            movementDto.setIdTransactionAssoc(transactions.get(i).getIdLoanAssoc());
            movementsDto.add(movementDto);
        }

        resumeMovementDto.setTotalNumberElements(transactions.size());
        resumeMovementDto.setTotalIN(totalIN);
        resumeMovementDto.setTotalOUT(totalOUT);
        resumeMovementDto.setMovememts(movementsDto);

        return resumeMovementDto;
    }

    @Override
    public List<Transaction> findByTypeAndStatusAndUserId(TypeEnum type, StatusEnum status, Long userId) {
        return this.transactionRepository.findByTypeAndStatusAndUserId(type, status, userId);
    }

    @Override
    public List<Transaction> findAllTxByUserId(Long userId) {
        return this.transactionRepository.findByUserId(userId);
    }


}
