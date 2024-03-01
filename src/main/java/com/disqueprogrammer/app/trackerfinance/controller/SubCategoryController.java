package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.SubCategory;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ISubCategoryService;
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
@Validated
@RequestMapping("/api/v2/workspace/{workspaceId}/subcategories")
public class SubCategoryController {

    private final ISubCategoryService subCategoryService;

    private final WorkspaceService workspaceService;
    @PostMapping
    public ResponseEntity<SubCategory> save(@PathVariable("workspaceId") Long workspaceId, @Valid  @RequestBody SubCategory segmentRequest) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        segmentRequest.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(subCategoryService.save(segmentRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubCategory> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idSubCategory, @Valid @RequestBody SubCategory segmentRequest) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        segmentRequest.setWorkspaceId(workspaceId);
        return new ResponseEntity<>(subCategoryService.update(segmentRequest, idSubCategory), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idSubCategory) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        subCategoryService.delete(idSubCategory, workspaceId);
        return new ResponseEntity<>("SubCategory was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<SubCategory>> findAll( @PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(subCategoryService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubCategory> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idSubCategory) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(subCategoryService.findByIdAndWorkspaceId(idSubCategory, workspaceId), HttpStatus.OK);
    }
}
