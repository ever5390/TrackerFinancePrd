package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ITransactionGetService {

    Transaction findByIdAndUserId(Long transactionId, Long userId) throws ObjectNotFoundException;

    public ResumeMovementDto findByUserId(Long userId) throws Exception;

    List<Transaction> findByTypeAndStatusAndUserId(TypeEnum type, StatusEnum status, Long userId);

    List<Transaction> findAllTxByUserId(Long userId);
}
