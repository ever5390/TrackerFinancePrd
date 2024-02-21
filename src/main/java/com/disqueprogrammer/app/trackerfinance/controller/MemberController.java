package com.disqueprogrammer.app.trackerfinance.controller;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Member;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IMemberService;
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
@RequestMapping("/api/v2/user/{userId}/members")
@Validated
public class MemberController {

    private final IMemberService memberService;

    private final AuthService authService;
    @PostMapping
    public ResponseEntity<Member> save(@PathVariable("userId") Long userId, @Valid @RequestBody Member memberReq) throws ObjectExistsException, ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        memberReq.setUserId(userId);
        return new ResponseEntity<>(memberService.save(memberReq), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> update(@PathVariable("userId") Long userId, @PathVariable("id") Long idMember, @Valid @RequestBody Member memberReq) throws ObjectExistsException, ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        memberReq.setUserId(userId);
        return new ResponseEntity<>(memberService.update(memberReq, idMember), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("userId") Long userId, @PathVariable("id") Long idMember) throws ObjectExistsException, ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        memberService.delete(idMember, userId);
        return new ResponseEntity<>("Member was deleted successfully!!", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Member>> findAll( @PathVariable("userId") Long userId) {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(memberService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> findById(@PathVariable("userId") Long userId, @PathVariable("id") Long idMember) throws ObjectNotFoundException {
        authService.validateUserIdRequestIsEqualsUserIdToken(userId);
        return new ResponseEntity<>(memberService.findByIdAndUserId(idMember, userId), HttpStatus.OK);
    }

}
