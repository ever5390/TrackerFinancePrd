package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByNameAndUserId(String name, Long userId);

    Account findByIdAndUserId(Long accountId, Long userId);

    List<Account> findByUserId(Long userId);

}
