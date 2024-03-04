package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.CardType;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.CardTypeService;
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
@RequestMapping("/api/v2/workspace/{workspaceId}/cardtype")
public class CardTypeController {

    private final CardTypeService cardTypeService;

    private final WorkspaceService workspaceService;


    public CardTypeController(CardTypeService cardTypeService, WorkspaceService workspaceService) {
        this.cardTypeService = cardTypeService;
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<CardType> save(@PathVariable("workspaceId") Long workspaceId, @Valid @RequestBody CardType accountReq) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        accountReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(cardTypeService.create(accountReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardType> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCardType, @Valid @RequestBody CardType accountReq) throws  CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        accountReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(cardTypeService.update(accountReq, idCardType), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCardType) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        cardTypeService.delete(idCardType, workspaceId);
        return (ResponseEntity<Void>) ResponseEntity.status(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardType> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCardType) throws Exception {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(cardTypeService.findCardTypeByIdAndWorkspaceId(idCardType, workspaceId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CardType>> findAll(@PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(cardTypeService.findAllByWorkspaceId(workspaceId), HttpStatus.OK);
    }
}
