package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Budget;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.BudgetRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.BudgetService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetServiceImpl implements BudgetService{

    BudgetRepository budgetRepository;

    TransactionRepository transactionRepository;
    public BudgetServiceImpl(BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget save(Budget budgetRequest) throws CustomException {

        //Validate limitAmount && dates range.
        validationBudgetLimitAmountAndDatesRanges(budgetRequest);

        //Update Tx by new categories
        List<Transaction> transactionsFounded = transactionRepository.findByWorkspaceIdAndCreateAtBetweenAndSubCategoryInAndBlock(budgetRequest.getWorkspaceId(), budgetRequest.getDateBegin(), budgetRequest.getDateEnd(), budgetRequest.getSubCategories(), BlockEnum.OUT);
        budgetRequest.setTransactions(transactionsFounded);

        BigDecimal usedAmount = BigDecimal.ZERO;
        for ( Transaction tx : transactionsFounded) {
            usedAmount= usedAmount.add(tx.getAmount());
        }

        if(budgetRequest.getDateEnd().isBefore(LocalDateTime.now()))
            budgetRequest.setHasAutomaticRegeneration(false);

        budgetRequest.setCode(String.valueOf(UUID.randomUUID()));
        budgetRequest.setUsedAmount(usedAmount);
        budgetRequest.setStatusOpen(true);

        return budgetRepository.save(budgetRequest);
    }

    private static void validationBudgetLimitAmountAndDatesRanges(Budget budget) throws CustomException {
        if(budget.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw  new CustomException("El monto debe ser de tipo numérico y mayor a 0.");

        if(budget.getDateEnd().isBefore(budget.getDateBegin()) || budget.getDateEnd().isEqual(budget.getDateBegin()))
            throw  new CustomException("La fecha final del presupuesto no puede ser menor o igual a su inicio.");
    }

    @Override
    public Budget regenerateNextBudget(Budget budgetRequest) throws CustomException {

        Budget budgetFounded = budgetRepository.findByIdAndWorkspaceId(budgetRequest.getId(), budgetRequest.getWorkspaceId());

        if(budgetFounded == null)
            throw  new CustomException("No se encontró el presupuesto a editar.");

        closeLastBudget(budgetFounded);

        // Regeneration new budget
        Budget newBudget = new Budget();
        newBudget.setStatusOpen(true);
        newBudget.setDetail(budgetFounded.getDetail());
        newBudget.setLimitAmount(budgetFounded.getLimitAmount());
        newBudget.setUsedAmount(BigDecimal.ZERO);
        newBudget.setCode(budgetFounded.getCode());
        newBudget.setDateBegin(budgetFounded.getDateEnd().plusDays(1));
        newBudget.setDateEnd(budgetFounded.getDateBegin().plusDays((int) ChronoUnit.DAYS.between(budgetFounded.getDateEnd(), budgetFounded.getDateBegin())));
        newBudget.setSubCategories(budgetFounded.getSubCategories());
        newBudget.setHasAutomaticRegeneration(budgetFounded.isHasAutomaticRegeneration());

        if(newBudget.getDateEnd().isBefore(LocalDateTime.now()))
            newBudget.setHasAutomaticRegeneration(false);

        return budgetRepository.save(newBudget);
    }

    private void closeLastBudget(Budget budgetRequest) throws CustomException {
        // Budget exists
        Budget budgetToClose = budgetRepository.findByIdAndWorkspaceId(budgetRequest.getId(), budgetRequest.getWorkspaceId());
        if(budgetToClose == null) throw  new CustomException("Ocurrió un problema al procesar el cierre del presupuesto anterior. Previo a generar el nuevo.");
        budgetToClose.setStatusOpen(false);
        budgetRepository.save(budgetToClose); //Update last budget
    }

    @Override
    public Budget findByIdAndWorkspaceId(Long budgetId, Long workspaceId) throws CustomException {
        return budgetRepository.findByIdAndWorkspaceId(budgetId, workspaceId);
    }

    @Override
    public List<Budget> findByWorkspaceId(Long workspaceId) {
        return budgetRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public Budget update(Budget budgetRequest, Long budgetId) throws CustomException {
        Budget budgetFounded = budgetRepository.findByIdAndWorkspaceId(budgetId, budgetRequest.getWorkspaceId());

        if(budgetFounded == null)
            throw  new CustomException("No se encontró el presupuesto a editar.");

        //Validate limitAmount && dates range.
        validationBudgetLimitAmountAndDatesRanges(budgetRequest);

        //Get last budget close to compare date init new and last DateEnd budget.
        List<Budget> budgetsFounded = budgetRepository.findByCodeAndWorkspaceIdOrderByDateEndDesc(budgetFounded.getCode(), budgetFounded.getWorkspaceId());
        if(!budgetsFounded.isEmpty() && (budgetsFounded.get(0).getDateEnd().isAfter(budgetRequest.getDateBegin()) || budgetsFounded.get(0).getDateEnd().isEqual(budgetRequest.getDateBegin())))
            throw  new CustomException("La nueva fecha de inicio cruza con el rango de fechas del último presupuesto, ingrese uno mayor a " + budgetsFounded.get(0).getDateEnd() + ".");

        //Update Tx by new categories
        List<Transaction> transactionsFounded = transactionRepository.findByWorkspaceIdAndCreateAtBetweenAndSubCategoryInAndBlock(budgetFounded.getWorkspaceId(), budgetRequest.getDateBegin(), budgetRequest.getDateEnd(), budgetRequest.getSubCategories(), BlockEnum.OUT);
        budgetFounded.setTransactions(transactionsFounded);

        BigDecimal usedAmount = BigDecimal.ZERO;
        for ( Transaction tx : transactionsFounded) {
            usedAmount=usedAmount.add(tx.getAmount());
        }

        budgetFounded.setUsedAmount(usedAmount);
        budgetFounded.setDetail(budgetRequest.getDetail());
        budgetFounded.setLimitAmount(budgetRequest.getLimitAmount());
        budgetFounded.setHasAutomaticRegeneration(budgetRequest.isHasAutomaticRegeneration());
        budgetFounded.setSubCategories(budgetRequest.getSubCategories());
        budgetFounded.setDateBegin(budgetRequest.getDateBegin());
        budgetFounded.setDateEnd(budgetRequest.getDateEnd());
        return budgetRepository.save(budgetRequest);
    }

    @Override
    public void delete(Long budgetId, Long workspaceId) throws CustomException {
        Budget budgetFounded = budgetRepository.findByIdAndWorkspaceId(budgetId, workspaceId);

        if(budgetFounded == null)
            throw  new CustomException("No se encontró el presupuesto a eliminar.");


        budgetRepository.deleteById(budgetId);
    }
}

