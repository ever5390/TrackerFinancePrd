package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.AccountRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.PaymentMethodRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IPaymentMethodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class PaymentMethodServiceImpl implements IPaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public PaymentMethod save(PaymentMethod paymentMethodRequest) throws ObjectExistsException, AccountNotFoundException {
        validateDuplicatedName(paymentMethodRequest);
        validateAccountAssoc(paymentMethodRequest);
        paymentMethodRequest.setName(paymentMethodRequest.getName().toUpperCase());
        paymentMethodRequest.setActive(true);
        return paymentMethodRepository.save(paymentMethodRequest);
    }

    private void validateAccountAssoc(PaymentMethod paymentMethodRequest) throws AccountNotFoundException {
        Account account = accountRepository.findByIdAndWorkspaceId(paymentMethodRequest.getAccount().getId(), paymentMethodRequest.getAccount().getWorkspaceId());
        if(account == null) {
            throw new AccountNotFoundException("La cuenta asociada no ha sido encontrada");
        }
    }

    @Override
    public PaymentMethod findByIdAndWorkspaceId(Long paymentMethodId, Long workspaceId) throws ObjectNotFoundException {

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndWorkspaceId(paymentMethodId, workspaceId);
        if(paymentMethod == null) {
            throw new ObjectNotFoundException("El medio de pago no ha sido encontrado");
        }
        return paymentMethod;
    }

    @Override
    public List<PaymentMethod> findByWorkspaceId(Long workspaceId) {
        return paymentMethodRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public PaymentMethod update(PaymentMethod paymentMethodRequest, Long paymentMethodId) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException {

        Optional<PaymentMethod> paymentMethodFounded = paymentMethodRepository.findById(paymentMethodId);
        if(paymentMethodFounded.isEmpty()) {
            throw new ObjectNotFoundException("El medio de pago seleccionado no ha sido encontrado");
        }

        if(!paymentMethodFounded.get().getName().equals(paymentMethodRequest.getName())) {
            validateDuplicatedName(paymentMethodRequest);
        }

        validateAccountAssoc(paymentMethodRequest);

        PaymentMethod paymentMethodToUpdate = paymentMethodFounded.get();
        paymentMethodToUpdate.setName(paymentMethodRequest.getName().toUpperCase());
        paymentMethodToUpdate.setAccount(paymentMethodRequest.getAccount());

        return paymentMethodRepository.save(paymentMethodToUpdate);
    }

    @Override
    public void delete(Long paymentMethodId, Long workspaceId) throws ObjectNotFoundException, CustomException {

        Optional<PaymentMethod> paymentMethodFounded = paymentMethodRepository.findById(paymentMethodId);
        if(paymentMethodFounded.isEmpty()) {
            throw new ObjectNotFoundException("El medio de pago seleccionado no ha sido encontrado");
        }

        List<Transaction> transactionsByPaymentMethodId = transactionRepository.findTransactionsByPaymentMethodIdAndWorkspaceId(paymentMethodId, workspaceId);

        if(!transactionsByPaymentMethodId.isEmpty()) {
            throw new CustomException("Se encontraron operaciones asociadas a este medio de pago, no es posible eliminar.");
        }

        paymentMethodRepository.deleteById(paymentMethodId);
    }
    
    private void validateDuplicatedName(PaymentMethod paymentMethodRequest) throws ObjectExistsException {

        PaymentMethod paymentMethodNameRepeated = paymentMethodRepository.findByNameAndWorkspaceId(paymentMethodRequest.getName().toUpperCase(), paymentMethodRequest.getAccount().getWorkspaceId());
        if(paymentMethodNameRepeated != null) {
            throw new ObjectExistsException("Ya existe un método de pago con el nombre que intentas registrar");
        }

    }
}
