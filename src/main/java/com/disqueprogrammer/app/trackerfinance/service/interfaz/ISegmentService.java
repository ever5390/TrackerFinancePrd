package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Segment;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;

import java.util.List;

public interface ISegmentService {
    Segment save(Segment segment) throws ObjectExistsException;

    Segment findByIdAndUserId(Long segmentId, Long userId) throws ObjectNotFoundException;

    List<Segment> findByUserId(Long userId);

    Segment update(Segment segment, Long idSegment) throws ObjectExistsException, ObjectNotFoundException;

    void delete(Long segmentId, Long userId) throws ObjectNotFoundException, CustomException;
}
