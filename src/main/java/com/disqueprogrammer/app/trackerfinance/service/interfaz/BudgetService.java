package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Budget;

import java.util.List;

public interface BudgetService {

    Budget save(Budget budget) throws CustomException;

    Budget regenerateNextBudget(Budget budget) throws CustomException;

    Budget findByIdAndWorkspaceId(Long budgetId, Long workspaceId) throws CustomException;

    List<Budget> findByWorkspaceId(Long workspaceId);

    Budget update(Budget budget, Long budgetId) throws CustomException;

    void delete(Long budgetId, Long workspaceId) throws CustomException;

}