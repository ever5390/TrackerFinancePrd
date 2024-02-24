package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    @Query("select p from PaymentMethod p where p.name =:paymentMethodName and p.account.workspaceId=:workspaceId")
    PaymentMethod findByNameAndWorkspaceId(String paymentMethodName, Long workspaceId);

    @Query("select p from PaymentMethod p where p.id =:paymentMethodId and p.account.workspaceId=:workspaceId")
    PaymentMethod findByIdAndWorkspaceId(Long paymentMethodId, Long workspaceId);

    @Query("select p from PaymentMethod p where p.account.workspaceId=:workspaceId")
    List<PaymentMethod> findByWorkspaceId(Long workspaceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentMethod pm WHERE pm.account.id = :idAccountAssoc")
    void deleteAllByIdAccount(Long idAccountAssoc);
}
