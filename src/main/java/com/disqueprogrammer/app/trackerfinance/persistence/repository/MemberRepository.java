package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {


    Member findByIdAndUserId(Long memberId, Long userId);
    Member findByEmailAndUserId(String email, Long userId);

    Member findByNameAndUserId(String name, Long userId);

    List<Member> findByUserId(Long userId);

}
