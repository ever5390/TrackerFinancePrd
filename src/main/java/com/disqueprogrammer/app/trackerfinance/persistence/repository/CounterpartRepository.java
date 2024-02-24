package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Counterpart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterpartRepository extends JpaRepository<Counterpart, Long> {

    Counterpart findByIdAndWorkspaceId(Long counterpartId, Long workspaceId);
    Counterpart findByEmailAndWorkspaceId(String email, Long workspaceId);

    Counterpart findByNameAndWorkspaceId(String name, Long workspaceId);

    List<Counterpart> findByWorkspaceId(Long workspaceId);

}
