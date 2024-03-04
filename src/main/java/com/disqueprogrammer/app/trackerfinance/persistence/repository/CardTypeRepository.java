package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardTypeRepository extends JpaRepository<CardType, Long> {

    List<CardType> findByWorkspaceId(Long workspaceId);

    CardType findByIdAndWorkspaceId(Long cardTypeId, Long workspaceId);

    CardType findByNameAndWorkspaceId(String name, Long workspaceId);
}
