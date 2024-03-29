package com.disqueprogrammer.app.trackerfinance.security.service;

import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.EmailExistsException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNameExistsException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNameNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.persistence.Role;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.AuthResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.LoginRequest;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.disqueprogrammer.app.trackerfinance.security.Jwt.JwtService;
import com.disqueprogrammer.app.trackerfinance.security.persistence.UserRepository;

import java.time.LocalDateTime;
import java.util.Date;

import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

	public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
    }
	
    public AuthResponse login(LoginRequest request) throws UserNameNotFoundException {
        //Empleamos el authentication Manager configurado @Bean, y le pasamos el UserNamePass... con los parámetros requeridos,
        //Este devuelve un tipo AuthenticationManager ya que lo implementa.
        //Internamente este AuthenticationManager utiliza una lista de mecanismos de authenticación y nosotros ya inyectamos el DaoAuthenticationProvider.

        LOGGER.info("Paso 0" + request.toString());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        LOGGER.info("Paso 1");
        User user = userRepository.findByUsername(request.getUsername()).get();
        LOGGER.info("Paso 2");
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);
        LOGGER.info("Paso 3");
        String token = jwtService.generateToken(user);
        LOGGER.info("Paso 4");
        return AuthResponse.builder()
                .token(token)
                .build();

    }

    public HttpResponse register(RegisterRequest request) throws UserNameExistsException, EmailExistsException {

        validateUserAndEmailExists(request.getUsername(), request.getEmail());
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode( request.getPassword()));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setCountry(request.getCountry());
        user.setJoinDate(LocalDateTime.now());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_ADMIN.name());
        user.setAuthorities(Role.ROLE_ADMIN.getAuthorities());
        userRepository.save(user);

        return new HttpResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase().toUpperCase().toString(),"Usuario creado exitosamente!!");

    }

    private void validateUserAndEmailExists(String username, String email) throws UserNameExistsException, EmailExistsException {

        Optional<User> userByUsername = userRepository.findByUsername(username);
        if(userByUsername.isPresent()) {
            throw new UserNameExistsException("Username already exists");
        }

        Optional<User> userbyEmail = userRepository.findByEmail(email);
        if(userbyEmail.isPresent()) {
            throw new EmailExistsException("Email already exists");
        }
    }

    public void validateUserIdRequestIsEqualsUserIdToken(Long userIdRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userAuthenticado = (User) authentication.getPrincipal();
        if(!Objects.equals(userAuthenticado.getId(), userIdRequest)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este recurso.");
        }
    }

}
