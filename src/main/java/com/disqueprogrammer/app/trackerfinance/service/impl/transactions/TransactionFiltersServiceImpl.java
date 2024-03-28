package com.disqueprogrammer.app.trackerfinance.service.impl.transactions;

import com.disqueprogrammer.app.trackerfinance.dto.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.SubCategory;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.AccountRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.CategoryRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.SubCategoryRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.specification.SearcherTransactionSpecification;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.ITransactionFiltersService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionFiltersServiceImpl implements ITransactionFiltersService {

    private final TransactionRepository transactionRepository;

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final AccountRepository accountRepository;

    public static final Logger LOGGER = LoggerFactory.getLogger(TransactionFiltersServiceImpl.class);


    public TransactionFiltersServiceImpl(TransactionRepository transactionRepository, CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.accountRepository = accountRepository;
    }

    private LocalDateTime getParseLocalDateIfValid(String dateTime) throws Exception {

        try {
            if(StringUtils.isNotBlank(dateTime)) {
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception("La fecha elegida no ha sido procesada correctamente por favor intente nuevamente");
        }

        return null;
    }

    @Override
    public FiltersDTO filterReload(Long workspaceId) {

        FiltersDTO filters = new FiltersDTO();

        List<Account> accounts = accountRepository.findByWorkspaceId(workspaceId);
        List<Category> categories = categoryRepository.findByWorkspaceId(workspaceId);
        List<SubCategory> subCategories = subCategoryRepository.findByWorkspaceId(workspaceId);

        ListItemFilter accountsFilters = new ListItemFilter();
        ListItemFilter categoriesFilters = new ListItemFilter();
        ListItemFilter subCategoriesFilters = new ListItemFilter();

        for (Account account: accounts) {
            ItemFilter accountItemFilter = new ItemFilter();
            accountItemFilter.setName(account.getName());
            accountsFilters.getItems().add(accountItemFilter);
        }

        for (Category category: categories) {
            ItemFilter categoryItemFilter = new ItemFilter();
            categoryItemFilter.setName(category.getName());
            categoriesFilters.getItems().add(categoryItemFilter);
        }

        for (SubCategory subCategory: subCategories) {
            ItemFilter subCategoryItemFilter = new ItemFilter();
            subCategoryItemFilter.setName(subCategory.getName());
            subCategoriesFilters.getItems().add(subCategoryItemFilter);
        }

        filters.setAccounts(accountsFilters);
        filters.setCategories(categoriesFilters);
        filters.setSubcategories(subCategoriesFilters);

        return filters;
    }

    @Override
    public ResumeMovementDto findMovementsByFilters2(Long workspaceIdParam, FiltersDTO filtersDTO) throws Exception {
        ResumeMovementDto resumeMovementDto = new ResumeMovementDto();

        LocalDateTime startDateD = getParseLocalDateIfValid(filtersDTO.getStartDate());
        LocalDateTime endDateD = getParseLocalDateIfValid(filtersDTO.getEndDate());

        filtersDTO.setWorkspaceIdParam(workspaceIdParam);
        filtersDTO.setStartDateLDT(startDateD==null?LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN):startDateD);
        filtersDTO.setEndDateLDT(endDateD==null?LocalDateTime.now():endDateD);

        LOGGER.info("::: startDateD: "+startDateD);
        LOGGER.info("::: endDateD: "+endDateD);

        SearcherTransactionSpecification specification = new SearcherTransactionSpecification(filtersDTO);

        List<Transaction> transactions = transactionRepository.findAll(specification);

        BigDecimal totalIN = BigDecimal.ZERO;
        BigDecimal totalOUT = BigDecimal.ZERO;
        BigDecimal totalTheyOweMe = BigDecimal.ZERO;
        BigDecimal totalIOweYou = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            BlockEnum blockIN_OUT = transaction.getBlock();
            BigDecimal amountMov = transaction.getAmount();

            if (blockIN_OUT.equals(BlockEnum.IN)) totalIN = totalIN.add(amountMov);

            if (blockIN_OUT.equals(BlockEnum.OUT)) totalOUT = totalOUT.add(amountMov);


            if(transaction.getType().equals(TypeEnum.LOAN) && transaction.getAction().equals(ActionEnum.REALICÉ)) {
                totalTheyOweMe = totalTheyOweMe.add(transaction.getRemaining());
            }

            if( (transaction.getType().equals(TypeEnum.LOAN) && transaction.getAction().equals(ActionEnum.RECIBÍ))
                    || (TypeEnum.EXPENSE.equals(transaction.getType()) && transaction.getAccount().getCardType().isFixedParameter())
                    || (TypeEnum.TRANSFERENCE.equals(transaction.getType()) && transaction.getAccount().getCardType().isFixedParameter()) ) {
                totalIOweYou = totalIOweYou.add(transaction.getRemaining());
            }

            //En caso se seleccione solo una cuenta entonces se contará cada transferencia como salida o entrada dependiendo de si es origen o receptor.

            List<ItemFilter> items = filtersDTO.getAccounts().getItems().stream().filter(ItemFilter::isSelected).toList();
            if (transaction.getType().equals(TypeEnum.TRANSFERENCE) && items.size() == 1) {
                if (filtersDTO.getAccounts().getItems().get(0).getName().equalsIgnoreCase(transaction.getAccount().getName()))
                    totalOUT = totalOUT.add(amountMov);

                if (filtersDTO.getAccounts().getItems().get(0).getName().equalsIgnoreCase(transaction.getAccountDestiny().getName()))
                    totalIN = totalIN.add(amountMov);
            }

        }

        resumeMovementDto.setTotalIOweYou(totalIOweYou);
        resumeMovementDto.setTotalTheyOweMe(totalTheyOweMe);
        resumeMovementDto.setTotalIN(totalIN);
        resumeMovementDto.setTotalOUT(totalOUT);
        resumeMovementDto.setMovememts(transactions);

        return resumeMovementDto;
    }

}
