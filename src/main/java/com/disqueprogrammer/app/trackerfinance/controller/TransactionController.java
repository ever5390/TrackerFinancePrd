package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedCounterpartException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
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
    public ResponseEntity<Transaction> save(@PathVariable("workspaceId") Long workspaceId, @RequestBody Transaction transactionRequest) throws UnspecifiedCounterpartException, AccountEqualsException, CustomException, InsuficientFundsException {
        LOG.info("inicio transacción");

        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        transactionRequest.setWorkspaceId(workspaceId);
        transactionRequest.setResponsableUser(workspaceService.getUserAuhenticated());
        return new ResponseEntity<>(transactionSaveService.save(transactionRequest), HttpStatus.CREATED);
    }

    @PostMapping("/recurring")
    public ResponseEntity<String> saveTxRecurring(@PathVariable("workspaceId") Long workspaceId, @RequestBody Transaction nextTransactionRecurring) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        nextTransactionRecurring.setWorkspaceId(workspaceId);
        nextTransactionRecurring.setResponsableUser(workspaceService.getUserAuhenticated());
        transactionSaveService.saveNewTransactionRecurring(nextTransactionRecurring);
        return new ResponseEntity<String>("La(s) transacciones fueron creada(s) satisfactoriamente", HttpStatus.CREATED);
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
    public ResponseEntity<String> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTransaction) throws CustomException, InsuficientFundsException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        transactionDeleteService.delete(idTransaction, workspaceId);
        return new ResponseEntity<>("Transaction was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable("id") Long idTransaction, @PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(transactionGetService.findByIdAndWorkspaceId(idTransaction, workspaceId), HttpStatus.OK);
    }

    @GetMapping("/filters")
    public ResponseEntity<ResumeMovementDto> findMovementsByFilters(
            @PathVariable("workspaceId") Long WorkspaceIdParam,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) TypeEnum type,
            @RequestParam(required = false) StatusEnum status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BlockEnum block,
            @RequestParam(required = false) ActionEnum action
    ) throws Exception {

        workspaceService.validationWorkspaceUserRelationship(WorkspaceIdParam);

        ResumeMovementDto resumeMovementDto = transactionFiltersService.findMovementsByFilters(WorkspaceIdParam, startDate,
                endDate, type, status, category, description, segment, account, paymentMethod, block, action);
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
    public ResponseEntity<List<Transaction>> findByTypeAndStatusAndWorkspaceId(@PathVariable("workspaceId") Long workspaceId, @RequestParam TypeEnum type, @RequestParam StatusEnum status) throws Exception {
        return new ResponseEntity<>(transactionGetService.findByTypeAndStatusAndWorkspaceId(type, status, workspaceId), HttpStatus.OK);
    }

}
