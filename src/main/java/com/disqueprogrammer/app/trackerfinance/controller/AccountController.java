package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NewBalanceLessThanCurrentBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.NotAllowedAccountBalanceException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.NotNumericException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.ExceptionHandling;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/v2/user/{userId}/accounts")
public class AccountController extends ExceptionHandling {

    Logger LOOGER = LoggerFactory.getLogger(AccountController.class);
    private final IAccountService accountService;

    //private final AuthService authService;

    public AccountController(IAccountService accountService, AuthService authService) {
        this.accountService = accountService;
        //this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Account> save(@PathVariable("userId") Long userId, @Valid @RequestBody Account accountReq) throws NotAllowedAccountBalanceException, NotNumericException, AccountNotFoundException, AccountExistsException, CustomException {
        //authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        accountReq.setUserId(userId);
        return new ResponseEntity<>(accountService.save(accountReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable("userId") Long userId, @PathVariable("id") Long idAccount, @Valid @RequestBody Account accountReq) throws NotAllowedAccountBalanceException, NotNumericException, NewBalanceLessThanCurrentBalanceException, AccountNotFoundException, AccountExistsException, CustomException {
        //authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        accountReq.setUserId(userId);
        return new ResponseEntity<>(accountService.update(accountReq, idAccount), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") Long userId, @PathVariable("id") Long idAccount) throws CustomException, AccountNotFoundException {
        //authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        accountService.delete(idAccount, userId);
        return new ResponseEntity<>("Account was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable("userId") Long userId, @PathVariable("id") Long idAccount) throws Exception {
        //authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(accountService.findByIdAndUserId(idAccount, userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Account>> findAll(@PathVariable("userId") Long userId){
        //authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(accountService.findByUserId(userId), HttpStatus.OK);
    }


}
