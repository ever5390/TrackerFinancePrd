package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.dto.FiltersDTO;
import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedCounterpartException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.WorkspaceService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@Validated
@RequestMapping("/api/v2/workspace/{workspaceId}/transactions")
public class TransactionController {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionController.class);

    private final ITransactionGetService transactionGetService;

    private final ITransactionSaveService transactionSaveService;

    private final ITransactionUpdateService transactionUpdateService;

    private final ITransactionDeleteService transactionDeleteService;

    private final ITransactionFiltersService transactionFiltersService;

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Transaction> save(@PathVariable("workspaceId") Long workspaceId, @RequestBody Transaction transactionRequest) throws UnspecifiedCounterpartException, CustomException, InsuficientFundsException, AccountEqualsException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        transactionRequest.setWorkspaceId(workspaceId);
        transactionRequest.setResponsableUser(workspaceService.getUserAuhenticated());
        return new ResponseEntity<>(transactionSaveService.save(transactionRequest), HttpStatus.CREATED);
    }

    @PostMapping("/recurring")
    public ResponseEntity<?> saveTxRecurring(@PathVariable("workspaceId") Long workspaceId, @RequestBody Transaction nextTransactionRecurring) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        nextTransactionRecurring.setWorkspaceId(workspaceId);
        nextTransactionRecurring.setResponsableUser(workspaceService.getUserAuhenticated());
        transactionSaveService.saveNewTransactionRecurring(nextTransactionRecurring);
        return ResponseEntity.ok().build();


        // return new ResponseEntity<String>("La(s) transacciones fueron creada(s) satisfactoriamente", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTransaction, @Valid @RequestBody Transaction transactionRequest) throws CustomException, InsuficientFundsException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        transactionRequest.setResponsableUser(workspaceService.getUserAuhenticated());
        return new ResponseEntity<>(transactionUpdateService.update(transactionRequest, idTransaction), HttpStatus.OK);
    }

    @PutMapping("/recurring/{id}")
    public ResponseEntity<Transaction> updateTxRecurring(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTransactionReqToUpdate, @RequestBody Transaction nextTransactionRecurring) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        nextTransactionRecurring.setWorkspaceId(workspaceId);
        nextTransactionRecurring.setResponsableUser(workspaceService.getUserAuhenticated());
        return new ResponseEntity<Transaction>(transactionUpdateService.updateTransactionRecurring(nextTransactionRecurring, idTransactionReqToUpdate), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTransaction) throws CustomException, InsuficientFundsException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        transactionDeleteService.delete(idTransaction, workspaceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable("id") Long idTransaction, @PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(transactionGetService.findByIdAndWorkspaceId(idTransaction, workspaceId), HttpStatus.OK);
    }

    @PostMapping("/filters")
    public ResponseEntity<ResumeMovementDto> findMovementsByFilters(
            @PathVariable("workspaceId") Long WorkspaceIdParam,
            @RequestBody FiltersDTO filtersDTO
    ) throws Exception {

        workspaceService.validationWorkspaceUserRelationship(WorkspaceIdParam);

        ResumeMovementDto resumeMovementDto = transactionFiltersService.findMovementsByFilters2(WorkspaceIdParam, filtersDTO);
        return new ResponseEntity<>(resumeMovementDto, HttpStatus.OK);

    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeMovementDto> findResumenByWorkspaceId(@PathVariable("workspaceId") Long workspaceId) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(transactionGetService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAllTxByWorkspaceId(@PathVariable("workspaceId") Long workspaceId) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<List<Transaction>>(transactionGetService.findAllTxByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/loans-pending")
    public ResponseEntity<List<Transaction>> findByStatusAndWorkspaceId(@PathVariable("workspaceId") Long workspaceId, @RequestParam StatusEnum status) throws Exception {
        return new ResponseEntity<>(transactionGetService.findByStatusAndWorkspaceId(status, workspaceId), HttpStatus.OK);
    }

    @GetMapping("/filter-reload")
    public ResponseEntity<FiltersDTO> filterReload(@PathVariable("workspaceId") Long workspaceId) throws Exception {
        return new ResponseEntity<FiltersDTO>(transactionFiltersService.filterReload(workspaceId), HttpStatus.OK);
    }

}
