package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedCounterpartException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;

public interface ITransactionSaveService {
    Transaction save(Transaction transaction) throws CustomException, InsuficientFundsException, UnspecifiedCounterpartException, AccountEqualsException;

    void saveNewTransactionRecurring(Transaction nextTransactionRecurring) throws Exception;
}
