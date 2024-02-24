package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByNameAndWorkspaceId(String name, Long workspaceId);

    Account findByIdAndWorkspaceId(Long accountId, Long workspaceId);

    List<Account> findByWorkspaceId(Long workspaceId);

}
