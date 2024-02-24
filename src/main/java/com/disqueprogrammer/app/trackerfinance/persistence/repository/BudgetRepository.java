package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Budget findByIdAndWorkspaceId(Long budgetId, Long workspaceId);

    List<Budget> findByWorkspaceId(Long workspaceId);

    List<Budget> findByCodeAndWorkspaceIdOrderByDateEndDesc(String code, Long workspaceId);
}
