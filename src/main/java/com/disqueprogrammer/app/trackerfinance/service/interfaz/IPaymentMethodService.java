package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;

import java.util.List;

public interface IPaymentMethodService {
    PaymentMethod save(PaymentMethod paymentMethod) throws ObjectExistsException, AccountNotFoundException;

    PaymentMethod findByIdAndWorkspaceId(Long paymentMethodId, Long workspaceId) throws ObjectNotFoundException;
    List<PaymentMethod> findByWorkspaceId(Long workspaceId);

    List<PaymentMethod> findByAccountIdAndWorkspaceId(Long accountId, Long workspaceId) throws AccountNotFoundException;

    PaymentMethod update(PaymentMethod paymentMethod, Long idPaymentMethod) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException;

    void delete(Long paymentMethodId, Long workspaceId) throws ObjectNotFoundException, CustomException;
}
