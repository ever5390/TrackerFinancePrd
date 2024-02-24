package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByNameAndWorkspaceId(String name, Long workspaceId);

    Category findByIdAndWorkspaceId(Long categorId, Long workspaceId);

    List<Category> findByWorkspaceId(Long workspaceId);
}
