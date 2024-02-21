package com.disqueprogrammer.app.trackerfinance.persistence.repository;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {
    Segment findByNameAndUserId(String segmentName, Long userId);

    Segment findByIdAndUserId(Long segmentId, Long userId);

    List<Segment> findByUserId(Long userId);

}
