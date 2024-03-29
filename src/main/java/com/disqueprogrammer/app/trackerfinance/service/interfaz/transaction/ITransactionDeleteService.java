package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;

import java.util.List;

public interface ITransactionDeleteService {
    void delete(Long transactionId, Long userId) throws CustomException, ObjectNotFoundException, InsuficientFundsException;
}
