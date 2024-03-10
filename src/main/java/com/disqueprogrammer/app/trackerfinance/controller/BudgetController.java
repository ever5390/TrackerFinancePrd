package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Budget;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.BudgetService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.WorkspaceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@RequestMapping("/api/v2/workspace/{workspaceId}/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Budget> save(@PathVariable("workspaceId") Long workspaceId, @Valid @RequestBody Budget budgetReq) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        budgetReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<Budget>(budgetService.save(budgetReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idBudget, @Valid @RequestBody Budget budgetReq) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        budgetReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<Budget>(budgetService.update(budgetReq, idBudget), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idBudget) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        budgetService.delete(idBudget, workspaceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Budget>> findAll(@PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<List<Budget>>(budgetService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idBudget) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(budgetService.findByIdAndWorkspaceId(idBudget, workspaceId), HttpStatus.OK);
    }
}
