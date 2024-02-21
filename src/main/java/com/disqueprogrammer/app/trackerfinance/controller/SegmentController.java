package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Segment;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ISegmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@Validated
@RequestMapping("/api/v2/user/{userId}/segments")
public class SegmentController {

    private final ISegmentService segmentService;

    private final AuthService authService;
    @PostMapping
    public ResponseEntity<Segment> save(@PathVariable("userId") Long userId, @Valid  @RequestBody Segment segmentRequest) throws ObjectExistsException, ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        segmentRequest.setUserId(userId);
        return new ResponseEntity<>(segmentService.save(segmentRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Segment> update(@PathVariable("userId") Long userId, @PathVariable("id") Long idSegment, @Valid @RequestBody Segment segmentRequest) throws ObjectExistsException, ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        segmentRequest.setUserId(userId);
        return new ResponseEntity<>(segmentService.update(segmentRequest, idSegment), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") Long userId, @PathVariable("id") Long idSegment) throws ObjectNotFoundException, CustomException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        segmentService.delete(idSegment, userId);
        return new ResponseEntity<>("Segment was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Segment>> findAll( @PathVariable("userId") Long userId) {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(segmentService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Segment> findById(@PathVariable("userId") Long userId, @PathVariable("id") Long idSegment) throws ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(segmentService.findByIdAndUserId(idSegment, userId), HttpStatus.OK);
    }
}
