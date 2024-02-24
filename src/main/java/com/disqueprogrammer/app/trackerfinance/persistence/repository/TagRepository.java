package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Tag findByTagNameAndWorkspaceId(String tagName, Long workspaceId);

    Tag findByIdAndWorkspaceId(Long Id , Long workspaceId);

    List<Tag> findByWorkspaceId(Long workspaceId);
}
