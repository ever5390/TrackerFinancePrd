package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Member;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;

import java.util.List;

public interface IMemberService {
    Member save(Member member) throws ObjectExistsException, ObjectNotFoundException;

    Member findByIdAndUserId(Long memberId, Long userId) throws ObjectNotFoundException;
    List<Member> findByUserId(Long userId);

    Member update(Member member, Long memberId) throws ObjectExistsException, ObjectNotFoundException;

    void delete(Long memberId, Long userId) throws ObjectExistsException, ObjectNotFoundException;
}
