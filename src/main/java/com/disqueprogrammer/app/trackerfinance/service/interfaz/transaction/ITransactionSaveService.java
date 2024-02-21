package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedMemberException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;

import java.util.List;

public interface ITransactionSaveService {
    Transaction save(Transaction transaction) throws CustomException, InsuficientFundsException, ObjectNotFoundException, UnspecifiedMemberException, AccountEqualsException;
}
