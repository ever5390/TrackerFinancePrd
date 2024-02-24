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
    public SubCategory save(SubCategory segmentRequest) throws CustomException {

        validateDuplicatedName(segmentRequest);

        SubCategory segmentNameRepeated = subCategoryRepository.findByNameAndWorkspaceId(segmentRequest.getName().toUpperCase(), segmentRequest.getWorkspaceId());
        if(segmentNameRepeated != null) {
            throw new CustomException("Ya existe un segmento con el nombre que intentas registrar");
        }

        segmentRequest.setName(segmentRequest.getName().toUpperCase());

        return subCategoryRepository.save(segmentRequest);
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
    public SubCategory update(SubCategory segmentRequest, Long idSubCategory) throws CustomException {

        SubCategory segmentFounded = subCategoryRepository.findByIdAndWorkspaceId(idSubCategory, segmentRequest.getWorkspaceId());
        if(segmentFounded == null) {
            throw new CustomException("El segmento seleccionado no ha sido encontrado");
        }

        if(!segmentFounded.getName().equals(segmentRequest.getName())) {
            validateDuplicatedName(segmentRequest);
        }

        segmentFounded.setName(segmentRequest.getName().toUpperCase());
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

    private void validateDuplicatedName(SubCategory segmentRequest) throws CustomException {

        SubCategory segmentNameRepeated = subCategoryRepository.findByNameAndWorkspaceId(segmentRequest.getName().toUpperCase(), segmentRequest.getWorkspaceId());
        if(segmentNameRepeated != null) {
            throw new CustomException("Ya existe un segmento con el nombre que intentas registrar");
        }

    }
}

