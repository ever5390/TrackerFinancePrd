package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.domain.CategoryNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.CategoryRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ICategoryService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CategoryServiceImpl implements ICategoryService {

    private final static Logger LOG = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    private final TransactionRepository transactionRepository;

    @Override
    public Category save(Category categoryRequest) throws CategoryExistsException {
        validateDuplicatedName(categoryRequest);
        categoryRequest.setName(categoryRequest.getName());
        return categoryRepository.save(categoryRequest);
    }

    @Override
    public Category findByIdAndWorkspaceId(Long categoryId, Long workspaceId) throws CategoryNotFoundException {

        Category category = categoryRepository.findByIdAndWorkspaceId(categoryId, workspaceId);

        if (category == null) {
            LOG.info(":::::::: No se encontró la categoría seleccionada :::::");
            throw new CategoryNotFoundException("No se encontró la categoría seleccionada");
        }

        return category;
    }

    @Override
    public List<Category> findByWorkspaceId(Long workspaceId) {
        return categoryRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public Category update(Category categoryRequest, Long idCategory) throws CategoryExistsException, CategoryNotFoundException {

        Optional<Category> optionalCategory = categoryRepository.findById(idCategory);

        if (optionalCategory.isEmpty()) {
            throw new CategoryNotFoundException("No se encontró la categoría que desea actualizar");
        }

        if(!optionalCategory.get().getName().equalsIgnoreCase(categoryRequest.getName())) {
            validateDuplicatedName(categoryRequest);
        }

        Category categoryToUpdate = optionalCategory.get();
        categoryToUpdate.setName(categoryRequest.getName().toLowerCase());

        return categoryRepository.save(categoryToUpdate);

    }

    @Override
    public void delete(Long categoryId, Long workspaceId) throws CategoryNotFoundException {
        Category optionalCategory = categoryRepository.findByIdAndWorkspaceId(categoryId, workspaceId);

        if (optionalCategory == null) {
            throw new CategoryNotFoundException("No se encontró la categoría que desea eliminar");
        }

        categoryRepository.deleteById(categoryId);
    }

    private void validateDuplicatedName(Category categoryRequest) throws CategoryExistsException {

        Category categoryNameRepeated = categoryRepository.findByNameAndWorkspaceId(categoryRequest.getName(), categoryRequest.getWorkspaceId());
        if(categoryNameRepeated != null) {
            throw new CategoryExistsException("Ya existe una categoría con el nombre que intentas registrar");
        }

    }
}
