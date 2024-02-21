package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("Select t from Transaction t where t.type = 'PAYMENT' and t.idLoanAssoc = :transactionLoanId and userId =:userId")
    List<Transaction> findPaymentsByLoanIdAssocAndUserId(Long transactionLoanId, Long userId);
    @Query("Select t from Transaction t where t.paymentMethod.account.id = :accountId and userId =:userId")
    List<Transaction> findTransactionsByAccountIdAndUserId(Long accountId, Long userId);

    @Query("Select t from Transaction t where t.member.id = :memberId and userId =:userId")
    List<Transaction> findTransactionsByMemberIdAndUserId(Long memberId, Long userId);

    @Query("Select t from Transaction t where t.paymentMethod.id = :paymentMethodId and userId =:userId")
    List<Transaction> findTransactionsByPaymentMethodIdAndUserId(Long paymentMethodId, Long userId);

    @Query("Select t from Transaction t where t.segment.id = :segmentId and userId =:userId")
    List<Transaction> findTransactionsBySegmentIdAndUserId(Long segmentId, Long userId);

    List<Transaction> findByUserId(Long userId);

    Transaction findByIdAndUserId(Long transactionId, Long userId);

    List<Transaction> findByTypeAndStatusAndUserId(TypeEnum type, StatusEnum status, Long userId);
}
