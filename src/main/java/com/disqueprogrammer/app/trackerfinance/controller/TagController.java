package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Tag;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.TagService;
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
@RequestMapping("/api/v2/workspace/{workspaceId}/tags")
public class TagController {

    private final TagService tagService;

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Tag> save(@PathVariable("workspaceId") Long workspaceId, @Valid @RequestBody Tag tagReq) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        tagReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<Tag>(tagService.save(tagReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tag> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTag, @Valid @RequestBody Tag tagReq) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        tagReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<Tag>(tagService.update(tagReq, idTag), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTag) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        tagService.delete(idTag, workspaceId);
        return new ResponseEntity<String>("Tag was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Tag>> findAll(@PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<List<Tag>>(tagService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idTag) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(tagService.findByIdAndWorkspaceId(idTag, workspaceId), HttpStatus.OK);
    }
}

