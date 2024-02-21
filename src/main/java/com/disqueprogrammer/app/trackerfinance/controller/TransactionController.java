package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountEqualsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.InsuficientFundsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.UnspecifiedMemberException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.impl.transactions.TransactionSaveServiceImpl;
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
@RequestMapping("/api/v2/user/{userId}/transactions")
public class TransactionController {

    private final static Logger LOG = LoggerFactory.getLogger(TransactionController.class);


    private final ITransactionGetService transactionGetService;

    private final ITransactionSaveService transactionSaveService;

    private final ITransactionUpdateService transactionUpdateService;

    private final ITransactionDeleteService transactionDeleteService;

    private final ITransactionFiltersService transactionFiltersService;

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<Transaction> save(@PathVariable("userId") Long userId, @RequestBody Transaction transactionRequest) throws UnspecifiedMemberException, ObjectNotFoundException, AccountEqualsException, CustomException, InsuficientFundsException {
        LOG.info("inicio transacción");

        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        transactionRequest.setUserId(userId);
        return new ResponseEntity<>(transactionSaveService.save(transactionRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable("userId") Long userId, @PathVariable("id") Long idTransaction, @Valid @RequestBody Transaction transactionRequest) throws ObjectNotFoundException, CustomException, InsuficientFundsException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(transactionUpdateService.update(transactionRequest, idTransaction), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") Long userId, @PathVariable("id") Long idTransaction) throws ObjectNotFoundException, CustomException, InsuficientFundsException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        transactionDeleteService.delete(idTransaction, userId);
        return new ResponseEntity<>("Transaction was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable("id") Long idTransaction, @PathVariable("userId") Long userId) throws ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(transactionGetService.findByIdAndUserId(idTransaction, userId), HttpStatus.OK);
    }

    @GetMapping("/filters")
    public ResponseEntity<ResumeMovementDto> findMovementsByFilters(
            @PathVariable("userId") Long userIdParam,
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

        authService.validateUserIdRequestIsEqualsUserIdToken(userIdParam);

        ResumeMovementDto resumeMovementDto = transactionFiltersService.findMovementsByFilters(userIdParam, startDate,
                endDate, type, status, category, description, segment, account, paymentMethod, block, action);
        return new ResponseEntity<>(resumeMovementDto, HttpStatus.OK);

    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeMovementDto> findResumenByUserId(@PathVariable("userId") Long userId) throws Exception {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(transactionGetService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAllTxByUserId(@PathVariable("userId") Long userId) throws Exception {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<List<Transaction>>(transactionGetService.findAllTxByUserId(userId), HttpStatus.OK);
    }


    @GetMapping("/loan-pending")
    public ResponseEntity<List<Transaction>> findByTypeAndStatusAndUserId(@PathVariable("userId") Long userId, @RequestParam TypeEnum type, @RequestParam StatusEnum status) throws Exception {
        return new ResponseEntity<>(transactionGetService.findByTypeAndStatusAndUserId(type, status, userId), HttpStatus.OK);
    }

}
