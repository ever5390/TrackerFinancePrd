package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ITransactionGetService {

    Transaction findByIdAndWorkspaceId(Long transactionId, Long workspaceId) throws CustomException;

    public ResumeMovementDto findByWorkspaceId(Long workspaceId) throws Exception;

    List<Transaction> findByTypeAndStatusAndWorkspaceId(TypeEnum type, StatusEnum status, Long workspaceId);

    List<Transaction> findAllTxByWorkspaceId(Long workspaceId);
}
