package com.disqueprogrammer.app.trackerfinance.persistence.specification;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class SearcherTransactionSpecification implements Specification<Transaction> {

    static final Logger LOG = LoggerFactory.getLogger(SearcherTransactionSpecification.class);

    private Long WorkspaceIdParam;
    private LocalDateTime startDate;
    private LocalDateTime  endDate;
    private TypeEnum type;
    private StatusEnum status;
    private String subCategory;
    private String description;
    private String segment;
    private String account;
    private String paymentMethod;
    private BlockEnum block;
    private ActionEnum action;


    @Override
    public Predicate toPredicate(Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        Expression<Long> transactionWorkspaceId = root.get("workspaceId");
        Predicate WorkspaceIdPredicate = criteriaBuilder.equal(transactionWorkspaceId, WorkspaceIdParam);
        predicates.add(WorkspaceIdPredicate);

        if (startDate != null) {
            Expression<LocalDateTime> transactionStartDate = root.get("createAt");
            Predicate startDatePredicate = criteriaBuilder.greaterThanOrEqualTo(transactionStartDate, startDate);
            predicates.add(startDatePredicate);
        }

        if(endDate != null) {
            Expression<LocalDateTime> transactionEndDate = root.get("createAt");
            Predicate endDatePredicate = criteriaBuilder.lessThanOrEqualTo(transactionEndDate, endDate);
            predicates.add(endDatePredicate);
        }

        if(status != null) {
            Expression<StatusEnum> transactionStatus = root.get("status");
            Predicate statusPredicate = criteriaBuilder.equal(transactionStatus, status);
            predicates.add(statusPredicate);
        }

        if(type != null) {
            Expression<TypeEnum> transactionType = root.get("type");
            Predicate typePredicate = criteriaBuilder.equal(transactionType, type);
            predicates.add(typePredicate);
        }

        if(block != null) {
            Expression<BlockEnum> transactionBlock = root.get("block");
            Predicate blockPredicate = criteriaBuilder.equal(transactionBlock, block);
            predicates.add(blockPredicate);
        }

        if(action != null) {
            Expression<ActionEnum> transactionAction = root.get("action");
            Predicate actionPredicate = criteriaBuilder.equal(transactionAction, action);
            predicates.add(actionPredicate);
        }

        if(StringUtils.hasText(description)){
            Expression<String> transactionDescriptionToLowerCase = criteriaBuilder.lower(root.get("description"));
            Predicate descriptionLikePredicate = criteriaBuilder.like(transactionDescriptionToLowerCase, "%".concat(description.toLowerCase()).concat("%"));
            predicates.add(descriptionLikePredicate);
        }


        if(StringUtils.hasText(subCategory)) {
            Join<Transaction, Category> transactionCategoryJoin = root.join("subCategory");
            Expression<String> categoryNameToLowerCase = criteriaBuilder.lower(transactionCategoryJoin.get("name"));
            Predicate categoryPredicate = criteriaBuilder.like(categoryNameToLowerCase, "%".concat(subCategory.toLowerCase()).concat("%"));
            predicates.add(categoryPredicate);
        }

/*
        if(StringUtils.hasText(segment)) {
            Join<Transaction, Segment> transactionSegmentJoin = root.join("segment");
            Expression<String> segmentNameToLowerCase = criteriaBuilder.lower(transactionSegmentJoin.get("name"));
            Predicate segmentPredicate = criteriaBuilder.like(segmentNameToLowerCase, "%".concat(segment.toLowerCase()).concat("%"));
            predicates.add(segmentPredicate);
        }
*/

        if (StringUtils.hasText(paymentMethod)) {
            Join<Transaction, PaymentMethod> transactionPaymentMethodJoin = root.join("paymentMethod", JoinType.LEFT);
            Join<Transaction, PaymentMethod> transactionPaymentMethodDestinyJoin = root.join("paymentMethodDestiny", JoinType.LEFT);
            Predicate paymentPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionPaymentMethodJoin.get("name")), paymentMethod.toLowerCase()),
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionPaymentMethodDestinyJoin.get("name")), paymentMethod.toLowerCase())
            );
            predicates.add(paymentPredicate);
        }

        if (StringUtils.hasText(account)) {
            Join<Transaction, PaymentMethod> transactionPaymentMethodJoin = root.join("paymentMethod", JoinType.LEFT);
            Join<Transaction, PaymentMethod> transactionPaymentMethodDestinyJoin = root.join("paymentMethodDestiny", JoinType.LEFT);
            Join<PaymentMethod, Account> paymentMethodAccountJoin = transactionPaymentMethodJoin.join("account", JoinType.LEFT);
            Join<PaymentMethod, Account> paymentMethodAccountDestinyJoin = transactionPaymentMethodDestinyJoin.join("account", JoinType.LEFT);
            Predicate accountPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(criteriaBuilder.lower(paymentMethodAccountJoin.get("name")), account.toLowerCase()),
                    criteriaBuilder.equal(criteriaBuilder.lower(paymentMethodAccountDestinyJoin.get("name")), account.toLowerCase())
            );
            predicates.add(accountPredicate);
        }

        query.orderBy(criteriaBuilder.desc(root.get("createAt")));

        return criteriaBuilder.and( predicates.toArray(new Predicate[0]) );
    }
}
