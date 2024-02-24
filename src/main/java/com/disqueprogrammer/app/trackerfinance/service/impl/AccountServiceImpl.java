package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NewBalanceLessThanCurrentBalanceException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NotAllowedAccountBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.NotNumericException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.AccountRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.PaymentMethodRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IAccountService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AccountServiceImpl implements IAccountService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentMethodRepository paymentRepository;

    @Override
    public Account save(Account accountRequest) throws NotAllowedAccountBalanceException, AccountNotFoundException, AccountExistsException, NotNumericException, CustomException {
        validateBalance(accountRequest);
        validateDuplicatedName(accountRequest);
        return accountRepository.save(accountRequest);
    }

    private void validateDuplicatedName(Account accountRequest) throws AccountExistsException, NotAllowedAccountBalanceException, AccountNotFoundException, CustomException {

        if(StringUtils.isEmpty(accountRequest.getName())) throw new CustomException("Ingrese un nombre para su cuenta por favor.");
        Account duplicatedNameAccount = accountRepository.findByNameAndWorkspaceId(accountRequest.getName().toUpperCase(), accountRequest.getWorkspaceId());

        if(duplicatedNameAccount != null) throw new AccountExistsException("Ya existe una cuenta con el nombre que intentas registrar");
    }

    private void validateBalance(Account accountRequest) throws NotNumericException, NotAllowedAccountBalanceException {
        if(accountRequest.getCurrentBalance() <= 0) throw new NotAllowedAccountBalanceException("La cuenta debe tener un monto asignado mayor a 0");
    }

    @Override
    public Account findByIdAndWorkspaceId(Long accountId, Long workspaceId) throws AccountNotFoundException {
        Account accountFounded = accountRepository.findByIdAndWorkspaceId(accountId, workspaceId);
        if (accountFounded == null) throw new AccountNotFoundException("No se encontró la cuenta seleccionada");
        return accountFounded;
    }

    @Override
    public List<Account> findByWorkspaceId(Long workspaceId) {
        return accountRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public Account update(Account accountRequest, Long idAccount) throws AccountNotFoundException, NotAllowedAccountBalanceException, AccountExistsException, NotNumericException, NewBalanceLessThanCurrentBalanceException, CustomException {

        Account accountFounded = accountRepository.findById(idAccount).orElseThrow(() -> new AccountNotFoundException("No se encontró la cuenta que intentas actualizar"));
        validateBalanceUpdate(accountRequest, accountFounded);
        if(!accountFounded.getName().equals(accountRequest.getName()))
            validateDuplicatedName(accountRequest);

        //Update params
        accountFounded.setName(accountRequest.getName().toUpperCase());
        accountFounded.setCurrentBalance(accountRequest.getCurrentBalance());
        return accountRepository.save(accountFounded);
    }

    @Override
    public void delete(Long accountId, Long workspaceId) throws AccountNotFoundException, CustomException {

        Account accountFounded = accountRepository.findByIdAndWorkspaceId(accountId, workspaceId);
        if(accountFounded == null ) throw new AccountNotFoundException("No se encontró la cuenta que intentas eliminar");

        validateIfExistTransactionsAssocThisAccount(accountFounded.getId(), workspaceId);

        //Deletes payments-methods assoc
        paymentRepository.deleteAllByIdAccount(accountFounded.getId());

        accountRepository.deleteById(accountFounded.getId());
    }

    private void validateIfExistTransactionsAssocThisAccount(Long accountId, Long workspaceId) throws CustomException {
        List<Transaction> transactionsByAccount = transactionRepository.findTransactionsByAccountIdAndWorkspaceId(accountId, workspaceId);
        if(!transactionsByAccount.isEmpty()) {
            throw new CustomException("Se encontraron operaciones asociadas a esta cuenta, no es posible eliminar la cuenta.");
        }
    }

    private void validateBalanceUpdate(Account accountRequest, Account accountFounded) throws NotAllowedAccountBalanceException, NewBalanceLessThanCurrentBalanceException {

        if(accountRequest.getCurrentBalance() <= 0) {
            throw new NotAllowedAccountBalanceException("La cuenta debe tener un monto asignado mayor a 0");
        }

        if(accountRequest.getCurrentBalance() < accountFounded.getCurrentBalance()) {
            throw new NewBalanceLessThanCurrentBalanceException("El balance de la cuenta a editar es menor al actual, agregue un monto mayor.");
        }
    }
}
