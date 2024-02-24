package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.SubCategory;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;

import java.util.List;

public interface ISubCategoryService {
    SubCategory save(SubCategory segment) throws CustomException;

    SubCategory findByIdAndWorkspaceId(Long segmentId, Long workspaceId) throws CustomException;

    List<SubCategory> findByWorkspaceId(Long workspaceId);

    SubCategory update(SubCategory segment, Long idSubCategory) throws CustomException;

    void delete(Long segmentId, Long workspaceId) throws CustomException;
}
