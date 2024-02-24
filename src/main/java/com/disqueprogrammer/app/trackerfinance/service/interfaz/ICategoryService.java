package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryNotFoundException;

import java.util.List;

public interface ICategoryService {

    Category save(Category category) throws CategoryExistsException;

    Category findByIdAndWorkspaceId(Long categoryId, Long workspaceId) throws CategoryNotFoundException;
    List<Category> findByWorkspaceId(Long workspaceId);

    Category update(Category category, Long idAccount) throws CategoryExistsException, CategoryNotFoundException;

    void delete(Long categoryId, Long workspaceId) throws CategoryNotFoundException;
}
