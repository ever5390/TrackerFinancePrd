package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NewBalanceLessThanCurrentBalanceException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.CardType;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NotAllowedAccountBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.NotNumericException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.AccountRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.CardTypeRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.PaymentMethodRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IAccountService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Transactional
@Service
public class AccountServiceImpl implements IAccountService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final CardTypeRepository cardTypeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentMethodRepository paymentRepository;

    @Override
    public Account save(Account accountRequest) throws NotAllowedAccountBalanceException, AccountNotFoundException, AccountExistsException, NotNumericException, CustomException {
        validateBalance(accountRequest);
        validateDuplicatedName(accountRequest);
        validateCardType(accountRequest.getCardType(), accountRequest.getWorkspaceId());
        accountRequest.setIcon(accountRequest.getIcon());
        accountRequest.setColor(accountRequest.getColor());
        accountRequest.setCardType(accountRequest.getCardType());
        accountRequest.setActive(true);
        accountRequest.setFixedParameter(false);
        Account accountSaved = accountRepository.save(accountRequest);
        setterPaymentMethodIfExist(accountRequest, accountSaved);

        return accountSaved;
    }

    private void setterPaymentMethodIfExist(Account accountRequest, Account accountSaved) {

        if(accountRequest.getId() != 0 && accountRequest.getPaymentMethods() != null) {
            for (PaymentMethod payment: accountRequest.getPaymentMethods()) {
                if(payment.getAccount() != null) {
                    payment.setAccount(null);
                    payment.setUsed(false);
                    paymentRepository.save(payment);
                }
            }
        }

        if(accountRequest.getPaymentMethods() != null) {
            for (PaymentMethod payment: accountRequest.getPaymentMethods()) {
                if(payment.getAccount() == null) {
                    payment.setUsed(true);
                    payment.setAccount(accountSaved);
                    paymentRepository.save(payment);
                }
            }
        }
    }

    private void validateCardType(CardType cardTypeReq, Long workspaceId) throws CustomException{
        if(cardTypeReq == null)
            throw new CustomException("Seleccione un tipo de tarjeta por favor.");

        CardType cardTypeFound = cardTypeRepository.findByIdAndWorkspaceId(cardTypeReq.getId(), workspaceId);
        if(cardTypeFound == null )
            throw new CustomException("El tipo de tarjeta asociado " + cardTypeReq.getName() + " no ha sido encontrado");
    }

    private void validateDuplicatedName(Account accountRequest) throws AccountExistsException, NotAllowedAccountBalanceException, AccountNotFoundException, CustomException {

        if(StringUtils.isEmpty(accountRequest.getName())) throw new CustomException("Ingrese un nombre para su cuenta por favor.");
        Account duplicatedNameAccount = accountRepository.findByNameAndWorkspaceId(accountRequest.getName(), accountRequest.getWorkspaceId());

        if(duplicatedNameAccount != null) throw new AccountExistsException("Ya existe una cuenta con el nombre que intentas registrar");
    }

    private void validateBalance(Account accountRequest) throws NotNumericException, NotAllowedAccountBalanceException {
        BigDecimal zero = BigDecimal.ZERO;
        int comparisonResult = accountRequest.getCurrentBalance().compareTo(zero);
        if(comparisonResult <= 0) throw new NotAllowedAccountBalanceException("La cuenta debe tener un monto asignado mayor a 0");
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

        validateBalanceUpdate(accountRequest);
        validateCardType(accountRequest.getCardType(), accountRequest.getWorkspaceId());
        setterPaymentMethodIfExist(accountRequest, accountFounded);
        accountRequest.setFixedParameter(false);
        if(!accountFounded.getName().equalsIgnoreCase(accountRequest.getName()))
            validateDuplicatedName(accountRequest);

        //Update params
        accountFounded.setPaymentMethods(accountRequest.getPaymentMethods());
        accountFounded.setActive(accountRequest.isActive());
        accountFounded.setIcon(accountRequest.getIcon());
        accountFounded.setColor(accountRequest.getColor());
        accountFounded.setName((accountFounded.isFixedParameter())?accountFounded.getName():accountRequest.getName());
        accountFounded.setCurrentBalance(accountRequest.getCurrentBalance());
        return accountRepository.save(accountFounded);
    }

    @Override
    public void delete(Long accountId, Long workspaceId) throws AccountNotFoundException, CustomException {

        Account accountFounded = accountRepository.findByIdAndWorkspaceId(accountId, workspaceId);
        if(accountFounded == null ) throw new AccountNotFoundException("No se encontró la cuenta que intentas eliminar");

        if(accountFounded.isFixedParameter() ) throw new CustomException("La cuenta inicial no puede ser borrada.");

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

    private void validateBalanceUpdate(Account accountRequest) throws NotAllowedAccountBalanceException {

        BigDecimal zero = BigDecimal.ZERO;
        int comparisonResult = accountRequest.getCurrentBalance().compareTo(zero);
        if(comparisonResult <= 0) {
            throw new NotAllowedAccountBalanceException("La cuenta debe tener un monto asignado mayor a 0");
        }

    }
}
