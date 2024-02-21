package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Segment;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.SegmentRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ISegmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SegmentServiceImpl implements ISegmentService {

    private final SegmentRepository segmentRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Segment save(Segment segmentRequest) throws ObjectExistsException, ObjectExistsException {

        validateDuplicatedName(segmentRequest);

        Segment segmentNameRepeated = segmentRepository.findByNameAndUserId(segmentRequest.getName().toUpperCase(), segmentRequest.getUserId());
        if(segmentNameRepeated != null) {
            throw new ObjectExistsException("Ya existe un segmento con el nombre que intentas registrar");
        }

        segmentRequest.setName(segmentRequest.getName().toUpperCase());

        return segmentRepository.save(segmentRequest);
    }

    @Override
    public Segment findByIdAndUserId(Long segmentId, Long userId) throws ObjectNotFoundException {

        Segment segmentFounded = segmentRepository.findByIdAndUserId(segmentId, userId);
        if(segmentFounded == null) {
            throw new ObjectNotFoundException("El segmento no ha sido encontrado");
        }
        return segmentFounded;
    }

    @Override
    public List<Segment> findByUserId(Long userId) {
        return segmentRepository.findByUserId(userId);
    }

    @Override
    public Segment update(Segment segmentRequest, Long idSegment) throws ObjectExistsException, ObjectNotFoundException {

        Segment segmentFounded = segmentRepository.findByIdAndUserId(idSegment, segmentRequest.getUserId());
        if(segmentFounded == null) {
            throw new ObjectNotFoundException("El segmento seleccionado no ha sido encontrado");
        }

        if(!segmentFounded.getName().equals(segmentRequest.getName())) {
            validateDuplicatedName(segmentRequest);
        }

        segmentFounded.setName(segmentRequest.getName().toUpperCase());
        return segmentRepository.save(segmentFounded);
    }

    @Override
    public void delete(Long segmentId, Long userId) throws ObjectNotFoundException, CustomException {

        Segment segmentFounded = segmentRepository.findByIdAndUserId(segmentId, userId);
        if(segmentFounded == null) {
            throw new ObjectNotFoundException("El segmento seleccionado no ha sido encontrado");
        }

        List<Transaction> transactionsBySegmentId = transactionRepository.findTransactionsBySegmentIdAndUserId(segmentId, userId);

        if(!transactionsBySegmentId.isEmpty()) {
            throw new CustomException("Se encontraron operaciones asociadas a este segmento, no es posible eliminar.");
        }
        segmentRepository.deleteById(segmentId);
    }

    private void validateDuplicatedName(Segment segmentRequest) throws ObjectExistsException {

        Segment segmentNameRepeated = segmentRepository.findByNameAndUserId(segmentRequest.getName().toUpperCase(), segmentRequest.getUserId());
        if(segmentNameRepeated != null) {
            throw new ObjectExistsException("Ya existe un segmento con el nombre que intentas registrar");
        }

    }
}
