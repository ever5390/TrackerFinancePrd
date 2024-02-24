package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryNotFoundException;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ICategoryService;
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
@RequestMapping("/api/v2/workspace/{workspaceId}/categories")
@Validated
public class CategoryController {

    private final ICategoryService categoryService;

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Category> save(@PathVariable("workspaceId") Long workspaceId, @Valid  @RequestBody Category categoryReq) throws CategoryExistsException, CategoryNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        categoryReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<Category>(categoryService.save(categoryReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCategory, @Valid @RequestBody Category categoryReq) throws CategoryExistsException, CategoryNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        categoryReq.setWorkspaceId(workspaceId);
        return new ResponseEntity<Category>(categoryService.update(categoryReq, idCategory), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCategory) throws CategoryNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        categoryService.delete(idCategory, workspaceId);
        return new ResponseEntity<String>("Category was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll(@PathVariable("workspaceId") Long workspaceId) throws CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<List<Category>>(categoryService.findByWorkspaceId(workspaceId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(@PathVariable("workspaceId") Long workspaceId, @PathVariable("id") Long idCategory) throws CategoryNotFoundException, CustomException {
        workspaceService.validationWorkspaceUserRelationship(workspaceId);
        return new ResponseEntity<>(categoryService.findByIdAndWorkspaceId(idCategory, workspaceId), HttpStatus.OK);
    }
}
