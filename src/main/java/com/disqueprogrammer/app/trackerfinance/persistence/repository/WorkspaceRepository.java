package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends CrudRepository<Workspace, Long> {

    Workspace findByName(String name);

    @Query("SELECT w FROM Workspace w JOIN w.users u WHERE u.id = :userId")
    List<Workspace> findWorkspacesByUserId(@Param("userId") Long userId);
}
