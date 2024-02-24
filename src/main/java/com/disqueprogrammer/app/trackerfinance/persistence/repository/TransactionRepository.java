package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.SubCategory;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("Select t from Transaction t where t.type = 'PAYMENT' and t.idLoanAssoc = :transactionLoanId and workspaceId =:workspaceId")
    List<Transaction> findPaymentsByLoanIdAssocAndWorkspaceId(Long transactionLoanId, Long workspaceId);
    @Query("Select t from Transaction t where t.paymentMethod.account.id = :accountId and workspaceId =:workspaceId")
    List<Transaction> findTransactionsByAccountIdAndWorkspaceId(Long accountId, Long workspaceId);

    @Query("Select t from Transaction t where t.counterpart.id = :counterpartId and workspaceId =:workspaceId")
    List<Transaction> findTransactionsByCounterPartIdAndWorkspaceId(Long counterpartId, Long workspaceId);

    @Query("Select t from Transaction t where t.paymentMethod.id = :paymentMethodId and workspaceId =:workspaceId")
    List<Transaction> findTransactionsByPaymentMethodIdAndWorkspaceId(Long paymentMethodId, Long workspaceId);

    @Query("Select t from Transaction t where t.subCategory.id = :subcategoryId and workspaceId =:workspaceId")
    List<Transaction> findTransactionsBySubCategoryIdAndWorkspaceId(Long subcategoryId, Long workspaceId);

    List<Transaction> findByWorkspaceId(Long workspaceId);

    Transaction findByIdAndWorkspaceId(Long transactionId, Long workspaceId);

    List<Transaction> findByTypeAndStatusAndWorkspaceId(TypeEnum type, StatusEnum status, Long workspaceId);

    //Budget
   // @Query("Select t from Transaction t where workspaceId =:workspaceId and between t.createAt = :beginDate and t.createAt =:endDate")
    List<Transaction> findByWorkspaceIdAndCreateAtBetweenAndSubCategoryInAndBlock(Long workspaceId, LocalDateTime beginDate, LocalDateTime endDate, List<SubCategory> subCategories, BlockEnum block);

}
