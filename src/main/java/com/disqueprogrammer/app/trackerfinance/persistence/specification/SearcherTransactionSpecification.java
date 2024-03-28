package com.disqueprogrammer.app.trackerfinance.persistence.specification;

import com.disqueprogrammer.app.trackerfinance.dto.FiltersDTO;
import com.disqueprogrammer.app.trackerfinance.dto.ItemFilter;
import com.disqueprogrammer.app.trackerfinance.dto.OrderFilterEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class SearcherTransactionSpecification implements Specification<Transaction> {

    static final Logger LOG = LoggerFactory.getLogger(SearcherTransactionSpecification.class);
/*
    private Long WorkspaceIdParam;
    private LocalDateTime startDate;
    private LocalDateTime  endDate;
    private TypeEnum type;
    private StatusEnum status;
    private String subCategory;
    private SubCategoriesDto subCategoriesTest;
    private String description;
    private String account;
    private String paymentMethod;
    private BlockEnum block;
    private ActionEnum action;
    private String responsableUser;
*/
    private FiltersDTO filtersDTO;

    @Override
    public Predicate toPredicate(Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        Expression<Long> transactionWorkspaceId = root.get("workspaceId");
        Predicate workspaceIdPredicate = criteriaBuilder.equal(transactionWorkspaceId, filtersDTO.getWorkspaceIdParam());
        predicates.add(workspaceIdPredicate);

        if (filtersDTO.getStartDateLDT() != null) {
            Expression<LocalDateTime> transactionStartDate = root.get("createAt");
            Predicate startDatePredicate = criteriaBuilder.greaterThanOrEqualTo(transactionStartDate, filtersDTO.getStartDateLDT());
            predicates.add(startDatePredicate);
        }

        if(filtersDTO.getEndDateLDT() != null) {
            Expression<LocalDateTime> transactionEndDate = root.get("createAt");
            Predicate endDatePredicate = criteriaBuilder.lessThanOrEqualTo(transactionEndDate, filtersDTO.getEndDateLDT());
            predicates.add(endDatePredicate);
        }
/*
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

        if(action != null) {
            Expression<ActionEnum> transactionAction = root.get("action");
            Predicate actionPredicate = criteriaBuilder.equal(transactionAction, action);
            predicates.add(actionPredicate);
        }

        if (subCategoriesTest.getSubcategories() != null && !subCategoriesTest.getSubcategories().isEmpty()) {
            Join<Transaction, SubCategory> transactionSubCategoryJoin = root.join("subCategory");
            Predicate[] predicatesSubCategories = subCategoriesTest.getSubcategories().stream()
                    .map(nombreCategoria -> {
                        Expression<String> categoryNameToLowerCase = criteriaBuilder.lower(transactionSubCategoryJoin.get("name"));
                        return criteriaBuilder.like(categoryNameToLowerCase, "%".concat(nombreCategoria.toLowerCase()).concat("%"));
                    })
                    .toArray(Predicate[]::new);
            predicates.add(criteriaBuilder.or(predicatesSubCategories));
        }


        if (StringUtils.hasText(responsableUser)) {
            Join<Transaction, User> transactionResposableUser = root.join("user", JoinType.LEFT);
            Predicate paymentPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionResposableUser.get("firstname")), responsableUser.toLowerCase())
            );
            predicates.add(paymentPredicate);
        }

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
            Join<Transaction, CardType> transactionCardTypeJoin = root.join("account", JoinType.LEFT);
            Join<Transaction, CardType> transactionCardTypeDestinyJoin = root.join("accountDestiny", JoinType.LEFT);
            Predicate paymentPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionCardTypeJoin.get("name")), account.toLowerCase()),
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionCardTypeDestinyJoin.get("name")), account.toLowerCase())
            );
            predicates.add(paymentPredicate);
        }


*/
        if(filtersDTO.getBlock() != null && filtersDTO.getBlock() != BlockEnum.NOT_APPLICABLE) {
            Expression<BlockEnum> transactionBlock = root.get("block");
            Predicate blockPredicate = criteriaBuilder.equal(transactionBlock, filtersDTO.getBlock());
            predicates.add(blockPredicate);
        }

        if(StringUtils.hasText(filtersDTO.getDescription())){
            Expression<String> transactionDescriptionToLowerCase = criteriaBuilder.lower(root.get("description"));
            Predicate descriptionLikePredicate = criteriaBuilder.like(transactionDescriptionToLowerCase, "%".concat(filtersDTO.getDescription().toLowerCase()).concat("%"));
            predicates.add(descriptionLikePredicate);
        }



        if (filtersDTO.getSubcategories().getItems() != null && !filtersDTO.getSubcategories().getItems().isEmpty()) {
            Join<Transaction, SubCategory> transactionSubCategoryJoin = root.join("subCategory");
            if(filtersDTO.getSubcategories().getOrderFilter().equals(OrderFilterEnum.INCLUDE)) {
                Predicate[] predicatesSubCategories = filtersDTO.getSubcategories().getItems().stream()
                        .filter(ItemFilter::isSelected) // === item -> item.isSelected() :: Filtrar solo los elementos activados
                        .map(itemSubCategory -> {
                            Expression<String> subCategoryNameToLowerCase = criteriaBuilder.lower(transactionSubCategoryJoin.get("name"));
                            return criteriaBuilder.like(subCategoryNameToLowerCase, "%".concat(itemSubCategory.getName().toLowerCase()).concat("%"));
                        })
                        .toArray(Predicate[]::new);
                predicates.add(criteriaBuilder.or(predicatesSubCategories));
            }

            if(filtersDTO.getSubcategories().getOrderFilter().equals(OrderFilterEnum.NOT_INCLUDE)) {
                Predicate[] predicatesSubCategories = filtersDTO.getSubcategories().getItems().stream()
                        .filter(ItemFilter::isSelected) // === item -> item.isSelected() :: Filtrar solo los elementos activados
                        .map(itemSubCategory -> {
                            Expression<String> subCategoryNameToLowerCase = criteriaBuilder.lower(transactionSubCategoryJoin.get("name"));
                            return criteriaBuilder.notLike(subCategoryNameToLowerCase, "%".concat(itemSubCategory.getName().toLowerCase()).concat("%"));
                        })
                        .toArray(Predicate[]::new);
                predicates.add(criteriaBuilder.and(predicatesSubCategories));
            }
        }

        if (filtersDTO.getCategories().getItems() != null && !filtersDTO.getCategories().getItems().isEmpty()) {
            Join<Transaction, SubCategory> transactionSubCategoryJoin = root.join("subCategory", JoinType.LEFT);
            Join<Transaction, Category> transactionCategoryJoin = transactionSubCategoryJoin.join("category", JoinType.LEFT);

            if(filtersDTO.getCategories().getOrderFilter().equals(OrderFilterEnum.INCLUDE)) {
                Predicate[] predicatesCategories = filtersDTO.getCategories().getItems().stream()
                        .filter(ItemFilter::isSelected) //  === item -> item.isSelected() :: Filtrar solo los elementos activados
                        .map(itemCategory -> {
                            Expression<String> categoryNameToLowerCase = criteriaBuilder.lower(transactionCategoryJoin.get("name"));
                            return criteriaBuilder.like(categoryNameToLowerCase, "%".concat(itemCategory.getName().toLowerCase()).concat("%"));
                        })
                        .toArray(Predicate[]::new);
                predicates.add(criteriaBuilder.or(predicatesCategories));

            }

            if(filtersDTO.getCategories().getOrderFilter().equals(OrderFilterEnum.NOT_INCLUDE)) {
                Predicate[] predicatesCategories = filtersDTO.getCategories().getItems().stream()
                        .filter(ItemFilter::isSelected) //  === item -> item.isSelected() :: Filtrar solo los elementos activados
                        .map(itemCategory -> {
                            Expression<String> categoryNameToLowerCase = criteriaBuilder.lower(transactionCategoryJoin.get("name"));
                            return criteriaBuilder.or(
                                    criteriaBuilder.isNull(transactionCategoryJoin),
                                    criteriaBuilder.notLike(categoryNameToLowerCase, "%".concat(itemCategory.getName().toLowerCase()).concat("%"))
                            );
                        })
                        .toArray(Predicate[]::new);
                predicates.add(criteriaBuilder.and(predicatesCategories));

            }
        }

        if (filtersDTO.getAccounts().getItems() != null && !filtersDTO.getAccounts().getItems().isEmpty()) {
            Join<Transaction, Account> transactionAccountJoin = root.join("account", JoinType.LEFT);
            Join<Transaction, Account> transactionAccountDestinyJoin = root.join("accountDestiny", JoinType.LEFT);

            if(filtersDTO.getAccounts().getOrderFilter().equals(OrderFilterEnum.INCLUDE)) {
                Predicate[] accountPredicates = filtersDTO.getAccounts().getItems().stream()
                        .filter(ItemFilter::isSelected) //  === item -> item.isSelected() :: Filtrar solo los elementos activados
                        .map(account -> {
                        /*
                            ::: Lo de abajo es lo mismo que:
                            Predicate accountPredicate = criteriaBuilder.or(
                                    criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountJoin.get("name")), account.getName().toLowerCase()),
                                    criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountDestinyJoin.get("name")), account.getName().toLowerCase())
                            );
                            return accountPredicate;
                        */
                            return criteriaBuilder.or(
                                    criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountJoin.get("name")), account.getName().toLowerCase()),
                                    criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountDestinyJoin.get("name")), account.getName().toLowerCase())
                            );
                        })
                        .toArray(Predicate[]::new);
                predicates.addAll(Arrays.asList(criteriaBuilder.or(accountPredicates)));
            }

            if(filtersDTO.getAccounts().getOrderFilter().equals(OrderFilterEnum.NOT_INCLUDE)) {
                Predicate[] accountPredicates = filtersDTO.getAccounts().getItems().stream()
                        .filter(ItemFilter::isSelected) //  === item -> item.isSelected() :: Filtrar solo los elementos activados
                        .map(account -> {
                            Predicate originNotEqualPredicate = criteriaBuilder.notEqual(
                                    criteriaBuilder.lower(transactionAccountJoin.get("name")),
                                    account.getName().toLowerCase()
                            );

                            Predicate destinyOrNullOrNotEqualPredicate = criteriaBuilder.or
                                    (
                                        criteriaBuilder.isNull(transactionAccountDestinyJoin),
                                        criteriaBuilder.notEqual(criteriaBuilder.lower(transactionAccountDestinyJoin.get("name")), account.getName().toLowerCase())
                                    );

                            return criteriaBuilder.and(originNotEqualPredicate, destinyOrNullOrNotEqualPredicate);

                        })
                        .toArray(Predicate[]::new);
                predicates.add(criteriaBuilder.and(accountPredicates));
            }


        }
/*


//:::::::::::

        if (filtersDTO.getAccounts().getItems() != null && !filtersDTO.getAccounts().getItems().isEmpty()) {
            Join<Transaction, Account> transactionAccountOriginJoin = root.join("account");
            Predicate[] predicatesAccountOrigin = filtersDTO.getAccounts().getItems().stream()
                    .map(itemAccountOrigin -> {
                        Expression<String> accountOriginNameToLowerCase = criteriaBuilder.lower(transactionAccountOriginJoin.get("name"));
                        return criteriaBuilder.like(accountOriginNameToLowerCase, "%".concat(itemAccountOrigin.getName().toLowerCase()).concat("%"));
                    })
                    .toArray(Predicate[]::new);
            predicates.add(criteriaBuilder.or(predicatesAccountOrigin));
        }

        if (filtersDTO.getAccounts().getItems() != null && !filtersDTO.getAccounts().getItems().isEmpty()) {
            Join<Transaction, Account> transactionAccountDestinyJoin = root.join("accountDestiny");
            Predicate[] predicatesAccountDestiny = filtersDTO.getAccounts().getItems().stream()
                    .map(itemAccountDestiny -> {
                        Expression<String> accountDestinyNameToLowerCase = criteriaBuilder.lower(transactionAccountDestinyJoin.get("name"));
                        return criteriaBuilder.like(accountDestinyNameToLowerCase, "%".concat(itemAccountDestiny.getName().toLowerCase()).concat("%"));
                    })
                    .toArray(Predicate[]::new);
            predicates.add(criteriaBuilder.or(predicatesAccountDestiny));
        }

        if (accounts != null && !accounts.isEmpty()) {
            List<Predicate> accountPredicates = new ArrayList<>();
            for (String account : accounts) {
                Join<Transaction, Account> transactionAccountJoin = root.join("account", JoinType.LEFT);
                Join<Transaction, Account> transactionAccountDestinyJoin = root.join("accountDestiny", JoinType.LEFT);
                Predicate accountPredicate = criteriaBuilder.or(
                        criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountJoin.get("name")), account.toLowerCase()),
                        criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountDestinyJoin.get("name")), account.toLowerCase())
                );
                accountPredicates.add(accountPredicate);
            }
            predicates.add(criteriaBuilder.or(accountPredicates.toArray(new Predicate[0])));
        }


        if (StringUtils.hasText(account)) {
            Join<Transaction, Account> transactionAccountJoin = root.join("account", JoinType.LEFT);
            Join<Transaction, Account> transactionAccountDestinyJoin = root.join("accountDestiny", JoinType.LEFT);
            Predicate paymentPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountJoin.get("name")), account.toLowerCase()),
                    criteriaBuilder.equal(criteriaBuilder.lower(transactionAccountDestinyJoin.get("name")), account.toLowerCase())
            );
            predicates.add(paymentPredicate);
        }
*/
        query.orderBy(criteriaBuilder.desc(root.get("createAt")));

        return criteriaBuilder.and( predicates.toArray(new Predicate[0]) );
    }
}
