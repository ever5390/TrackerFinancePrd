package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Workspace;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNotFoundException;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.WorkspaceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@RequestMapping("/api/v2/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Workspace> createWorkspace(@RequestBody Workspace workspaceReq) throws CustomException, UserNotFoundException {
        return new ResponseEntity<Workspace>(workspaceService.createWorkspace(workspaceReq), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Workspace>> findAll(@PathVariable("userId") Long userId) throws CustomException, UserNotFoundException {
        return new ResponseEntity<List<Workspace>>(workspaceService.findAllByUserId(userId), HttpStatus.OK);
    }

    @PutMapping("/associate-users")
    public ResponseEntity<Workspace> associateUsersToWorkspace(@RequestBody Workspace workspaceReq) throws CustomException, UserNotFoundException {
        return new ResponseEntity<Workspace>(workspaceService.associateUsersToWorkspace(workspaceReq), HttpStatus.CREATED);
    }

    @DeleteMapping("/{workspaceId}")
    public  ResponseEntity<String> deleteById(@PathVariable("userParentId") Long userParentId, @PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.deleteById(userParentId, workspaceId);
        return new ResponseEntity<>("Workspace was deleted successfully!!", HttpStatus.OK);
    }
}
