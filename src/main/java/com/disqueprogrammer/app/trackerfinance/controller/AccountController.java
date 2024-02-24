package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NewBalanceLessThanCurrentBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NotAllowedAccountBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.NotNumericException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.ExceptionHandling;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IAccountService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.WorkspaceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/v2/workspace/{workspaceId}/accounts")
public class AccountController extends ExceptionHandling {

    Logger LOOGER = LoggerFactory.getLogger(AccountController.class);
    private final IAccountService accountService;

    private final WorkspaceService workspaceService;


    public AccountController(IAccountService accountService, WorkspaceService workspaceService) {
        this.accountService = accountService;
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<Account> save(@PathVariable("workspaceId") Long workspaceId, @Valid @RequestBody Account accountReq) throws NotAllowedAccountBalanceException, NotNumericException, AccountNotFoundException, AccountExistsException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        accountReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(accountService.save(accountReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idAccount, @Valid @RequestBody Account accountReq) throws NotAllowedAccountBalanceException, NotNumericException, NewBalanceLessThanCurrentBalanceException, AccountNotFoundException, AccountExistsException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        accountReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(accountService.update(accountReq, idAccount), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idAccount) throws CustomException, AccountNotFoundException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        accountService.delete(idAccount, workspaceId);
        return new ResponseEntity<>("Account was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idAccount) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(accountService.findByIdAndWorkspaceId(idAccount, workspaceId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Account>> findAll(@PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(accountService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

}
