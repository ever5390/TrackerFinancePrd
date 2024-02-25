package com.disqueprogrammer.app.trackerfinance.security.controller;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.*;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.ExceptionHandling;
import com.disqueprogrammer.app.trackerfinance.security.Jwt.JwtService;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.security.service.UserService;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.AuthResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.LoginRequest;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@RequestMapping("/auth")
public class AuthController extends ExceptionHandling {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final AuthService authService;

    private final UserService userService;

    public AuthController(AuthService authService, JwtService jwtService, UserService userService) {
		this.authService = authService;
        this.userService = userService;
    }

	@PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) throws UserNameNotFoundException {
        LOGGER.info("::::::: LOGGER ::::::");
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "register")
    public ResponseEntity<HttpResponse> register(@RequestBody RegisterRequest request) throws EmailNotFoundException, UserNameNotFoundException {
        return new ResponseEntity<>(userService.registerUserSuperAdmin(request), HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated() and hasRole('ROLE_SUPER_ADMIN')")
    @PostMapping("/users/{userParentId}/newUser")
    public ResponseEntity<HttpResponse> registerNewUser(@PathVariable("userParentId") Long userParentId, @RequestBody RegisterRequest request) throws UserNameExistsException, EmailExistsException, CustomException {
        return new ResponseEntity<>(userService.registerNewUser(userParentId, request), HttpStatus.CREATED);
    }

    //@PreAuthorize("isAuthenticated() and hasAuthority('user:create')")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping("/users/{userParentId}")
    public List<User> findByUserParent(@PathVariable("userParentId") Long userParentId) throws UserNotFoundException {
        return this.userService.findByUserParent(userParentId);
    }

    @GetMapping("/users/all")
    public List<User> showUsers() {
        return this.userService.showUsers();
    }

}
