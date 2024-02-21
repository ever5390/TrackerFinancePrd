package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Member;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    @Query("select p from PaymentMethod p where p.name =:paymentMethodName and p.account.userId=:userId")
    PaymentMethod findByNameAndUserId(String paymentMethodName, Long userId);

    @Query("select p from PaymentMethod p where p.id =:paymentMethodId and p.account.userId=:userId")
    PaymentMethod findByIdAndUserId(Long paymentMethodId, Long userId);

    @Query("select p from PaymentMethod p where p.account.userId=:userId")
    List<PaymentMethod> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentMethod pm WHERE pm.account.id = :idAccountAssoc")
    void deleteAllByIdAccount(Long idAccountAssoc);
}
