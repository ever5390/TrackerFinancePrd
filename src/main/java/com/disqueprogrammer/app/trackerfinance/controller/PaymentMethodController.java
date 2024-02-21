package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.ExceptionHandling;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IPaymentMethodService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@RequestMapping("/api/v2/user/{userId}/payment-methods")
@Validated
public class PaymentMethodController extends ExceptionHandling {

    private final IPaymentMethodService paymentMethodService;

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<PaymentMethod> save(@PathVariable("userId") Long userId, @Valid  @RequestBody PaymentMethod memberReq) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        memberReq.getAccount().setUserId(userId);
        return new ResponseEntity<>(paymentMethodService.save(memberReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethod> update(@PathVariable("userId") Long userId, @PathVariable("id") Long idPaymentMethod, @Valid @RequestBody PaymentMethod memberReq) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        memberReq.getAccount().setUserId(userId);
        return new ResponseEntity<>(paymentMethodService.update(memberReq, idPaymentMethod), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") Long userId, @PathVariable("id") Long idPaymentMethod) throws ObjectNotFoundException, CustomException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        paymentMethodService.delete(idPaymentMethod, userId);
        return new ResponseEntity<>("PaymentMethod was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> findAll(@PathVariable("userId") Long userId) {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(paymentMethodService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> findById(@PathVariable("userId") Long userId, @PathVariable("id") Long idPaymentMethod) throws ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(paymentMethodService.findByIdAndUserId(idPaymentMethod, userId), HttpStatus.OK);
    }
}