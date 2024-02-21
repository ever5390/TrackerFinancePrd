package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;

import java.util.List;

public interface IPaymentMethodService {
    PaymentMethod save(PaymentMethod paymentMethod) throws ObjectExistsException, AccountNotFoundException;

    PaymentMethod findByIdAndUserId(Long paymentMethodId, Long userId) throws ObjectNotFoundException;
    List<PaymentMethod> findByUserId(Long userId);

    PaymentMethod update(PaymentMethod paymentMethod, Long idPaymentMethod) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException;

    void delete(Long paymentMethodId, Long userId) throws ObjectNotFoundException, CustomException;
}
