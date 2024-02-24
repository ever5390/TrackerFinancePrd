package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    SubCategory findByNameAndWorkspaceId(String segmentName, Long workspaceId);

    SubCategory findByIdAndWorkspaceId(Long segmentId, Long workspaceId);

    List<SubCategory> findByWorkspaceId(Long workspaceId);

}
