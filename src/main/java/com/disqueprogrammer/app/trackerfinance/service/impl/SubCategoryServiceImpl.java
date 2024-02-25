package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.SubCategory;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.SubCategoryRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ISubCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SubCategoryServiceImpl implements ISubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public SubCategory save(SubCategory subCategoryRequest) throws CustomException {

        validateDuplicatedName(subCategoryRequest);

        SubCategory segmentNameRepeated = subCategoryRepository.findByNameAndWorkspaceId(subCategoryRequest.getName().toUpperCase(), subCategoryRequest.getWorkspaceId());
        if(segmentNameRepeated != null) {
            throw new CustomException("Ya existe un segmento con el nombre que intentas registrar");
        }
        subCategoryRequest.setActive(true);
        subCategoryRequest.setName(subCategoryRequest.getName().toUpperCase());

        return subCategoryRepository.save(subCategoryRequest);
    }

    @Override
    public SubCategory findByIdAndWorkspaceId(Long segmentId, Long workspaceId) throws CustomException {

        SubCategory segmentFounded = subCategoryRepository.findByIdAndWorkspaceId(segmentId, workspaceId);
        if(segmentFounded == null) {
            throw new CustomException("El segmento no ha sido encontrado");
        }
        return segmentFounded;
    }

    @Override
    public List<SubCategory> findByWorkspaceId(Long workspaceId) {
        return subCategoryRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public SubCategory update(SubCategory subCategoryRequest, Long idSubCategory) throws CustomException {

        SubCategory segmentFounded = subCategoryRepository.findByIdAndWorkspaceId(idSubCategory, subCategoryRequest.getWorkspaceId());
        if(segmentFounded == null) {
            throw new CustomException("El segmento seleccionado no ha sido encontrado");
        }

        if(!segmentFounded.getName().equals(subCategoryRequest.getName())) {
            validateDuplicatedName(subCategoryRequest);
        }

        segmentFounded.setName(subCategoryRequest.getName().toUpperCase());
        return subCategoryRepository.save(segmentFounded);
    }

    @Override
    public void delete(Long segmentId, Long workspaceId) throws CustomException {

        SubCategory segmentFounded = subCategoryRepository.findByIdAndWorkspaceId(segmentId, workspaceId);
        if(segmentFounded == null) {
            throw new CustomException("El segmento seleccionado no ha sido encontrado");
        }

        List<Transaction> transactionsBySubCategoryId = transactionRepository.findTransactionsBySubCategoryIdAndWorkspaceId(segmentId, workspaceId);

        if(!transactionsBySubCategoryId.isEmpty()) {
            throw new CustomException("Se encontraron operaciones asociadas a este segmento, no es posible eliminar.");
        }
        subCategoryRepository.deleteById(segmentId);
    }

    private void validateDuplicatedName(SubCategory subCategoryRequest) throws CustomException {

        SubCategory segmentNameRepeated = subCategoryRepository.findByNameAndWorkspaceId(subCategoryRequest.getName().toUpperCase(), subCategoryRequest.getWorkspaceId());
        if(segmentNameRepeated != null) {
            throw new CustomException("Ya existe un segmento con el nombre que intentas registrar");
        }

    }
}

