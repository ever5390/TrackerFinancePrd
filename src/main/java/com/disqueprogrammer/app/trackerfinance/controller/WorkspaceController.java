package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Budget;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Workspace;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNotFoundException;
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
@RequestMapping("/api/v2/user/{userParentId}/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Workspace> save(@PathVariable("userParentId") Long userParentId, @RequestBody Workspace workspaceReq) throws CustomException, UserNotFoundException {
        return new ResponseEntity<Workspace>(workspaceService.save(workspaceReq, userParentId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Workspace>> findAll(@PathVariable("userParentId") Long userId) throws CustomException, UserNotFoundException {
        return new ResponseEntity<List<Workspace>>(workspaceService.findAllByUserId(userId), HttpStatus.CREATED);
    }

    @PutMapping("/{workspaceId}/user-assoc/{userAssocId}")
    public ResponseEntity<Workspace> saveAssocOneUserWorkspaceRelationship(@PathVariable("userParentId") Long userParentId, @PathVariable("userAssocId") Long userAssocId, @PathVariable("workspaceId") Long workspaceId) throws CustomException, UserNotFoundException {
        return new ResponseEntity<Workspace>(workspaceService.saveAssocOneUserWorkspaceRelationship(userParentId, userAssocId, workspaceId), HttpStatus.CREATED);
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<Workspace> saveAssocUsersWorkspaceRelationship(@PathVariable("userParentId") Long userParentId, @PathVariable("workspaceId") Long workspaceId, @RequestBody Workspace workspaceReq) throws CustomException, UserNotFoundException {
        return new ResponseEntity<Workspace>(workspaceService.saveAssocUsersWorkspaceRelationship(userParentId, workspaceId, workspaceReq), HttpStatus.CREATED);
    }
}
