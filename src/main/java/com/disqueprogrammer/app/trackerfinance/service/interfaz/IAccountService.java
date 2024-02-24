package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NewBalanceLessThanCurrentBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NotAllowedAccountBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.NotNumericException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;

import java.util.List;

public interface IAccountService {
    Account save(Account account) throws NotAllowedAccountBalanceException, AccountNotFoundException, AccountExistsException, NotNumericException, CustomException;

    Account findByIdAndWorkspaceId(Long accountId, Long workspaceId) throws AccountNotFoundException;
    List<Account> findByWorkspaceId(Long workspaceId);

    Account update(Account account,  Long idAccount) throws AccountNotFoundException, NotAllowedAccountBalanceException, AccountExistsException, NotNumericException, NewBalanceLessThanCurrentBalanceException, CustomException;

    void delete(Long accountId,  Long idAccount) throws AccountNotFoundException, CustomException;
}
