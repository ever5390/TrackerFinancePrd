package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Counterpart;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ICounterpartService;
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
@RequestMapping("/api/v2/workspace/{workspaceId}/counterparts")
@Validated
public class CounterpartController {

    private final ICounterpartService counterpartService;

    private final WorkspaceService workspaceService;
    @PostMapping
    public ResponseEntity<Counterpart> save(@PathVariable("workspaceId") Long workspaceId, @Valid @RequestBody Counterpart counterpartReq) throws ObjectExistsException, ObjectNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        counterpartReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(counterpartService.save(counterpartReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Counterpart> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCounterpart, @Valid @RequestBody Counterpart counterpartReq) throws ObjectExistsException, ObjectNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        counterpartReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(counterpartService.update(counterpartReq, idCounterpart), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCounterpart) throws ObjectExistsException, ObjectNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        counterpartService.delete(idCounterpart, workspaceId);
        return (ResponseEntity<Void>) ResponseEntity.status(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<Counterpart>> findAll( @PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(counterpartService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Counterpart> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCounterpart) throws ObjectNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(counterpartService.findByIdAndWorkspaceId(idCounterpart, workspaceId), HttpStatus.OK);
    }

}
