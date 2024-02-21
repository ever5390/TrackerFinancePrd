package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ICategoryService;
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
@RequestMapping("/api/v2/user/{userId}/categories")
@Validated
public class CategoryController {

    private final ICategoryService categoryService;

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<Category> save(@PathVariable("userId") Long userId, @Valid  @RequestBody Category categoryReq) throws CategoryExistsException, CategoryNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        categoryReq.setUserId(userId);
        return new ResponseEntity<Category>(categoryService.save(categoryReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable("userId") Long userId, @PathVariable("id") Long idCategory, @Valid @RequestBody Category categoryReq) throws CategoryExistsException, CategoryNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        categoryReq.setUserId(userId);
        return new ResponseEntity<Category>(categoryService.update(categoryReq, idCategory), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") Long userId, @PathVariable("id") Long idCategory) throws CategoryNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        categoryService.delete(idCategory, userId);
        return new ResponseEntity<String>("Category was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll(@PathVariable("userId") Long userId) {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<List<Category>>(categoryService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(@PathVariable("userId") Long userId, @PathVariable("id") Long idCategory) throws CategoryNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(categoryService.findByIdAndUserId(idCategory, userId), HttpStatus.OK);
    }
}
