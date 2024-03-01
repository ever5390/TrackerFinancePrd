package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.domain.AccountNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.PaymentMethod;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.ExceptionHandling;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IPaymentMethodService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.WorkspaceService;
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
@RequestMapping("/api/v2/workspace/{workspaceId}/payment-methods")
@Validated
public class PaymentMethodController extends ExceptionHandling {

    private final IPaymentMethodService paymentMethodService;

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<PaymentMethod> save(@PathVariable("workspaceId") Long workspaceId, @Valid  @RequestBody PaymentMethod paymentMethodReq) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        paymentMethodReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(paymentMethodService.save(paymentMethodReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethod> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idPaymentMethod, @Valid @RequestBody PaymentMethod paymentMethodReq) throws ObjectExistsException, ObjectNotFoundException, AccountNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        paymentMethodReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(paymentMethodService.update(paymentMethodReq, idPaymentMethod), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idPaymentMethod) throws ObjectNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        paymentMethodService.delete(idPaymentMethod, workspaceId);
        return new ResponseEntity<>("PaymentMethod was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> findAll(@PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(paymentMethodService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<List<PaymentMethod>> findByAccountId(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idAccount) throws ObjectNotFoundException, CustomException, AccountNotFoundException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(paymentMethodService.findByAccountIdANdWorkspaceId(idAccount, workspaceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idPaymentMethod) throws ObjectNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(paymentMethodService.findByIdAndWorkspaceId(idPaymentMethod, workspaceId), HttpStatus.OK);
    }
}