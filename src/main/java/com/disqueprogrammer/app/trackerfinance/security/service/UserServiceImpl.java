package com.disqueprogrammer.app.trackerfinance.security.service;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.*;
import com.disqueprogrammer.app.trackerfinance.security.persistence.Role;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.persistence.UserRepository;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final AuthService authService;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, AuthService authService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public List<User> showUsers() {
        return this.userRepository.findAll();
    }
    //Optional<User> userToUpdateFound = userRepository.findById(request.getUsername());

    public HttpResponse registerNewUser(Long userParentId, RegisterRequest request) throws UserNameExistsException, EmailExistsException, CustomException {

        //Valid if exists userParent
        Optional<User> userByParentUser = userRepository.findById(Math.toIntExact(userParentId));
        if(userByParentUser.isEmpty()) {
            throw new CustomException("El usuario padre no existe");
        }

        // user role == super_admin
        if(!userByParentUser.get().getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No tienes permisos para efectuar esta operación.");

        // userAssoc dont has role super_admin
        if(request.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No es posible crear más de un usuario padre, ingrese otro rol por favor.");

        Optional<User> userByUsername = userRepository.findByUsername(request.getUsername());
        if(userByUsername.isPresent()) {
            throw new UserNameExistsException("Username already exists");
        }

        Optional<User> userbyEmail = userRepository.findByEmail(request.getEmail());
        if(userbyEmail.isPresent()) {
            throw new EmailExistsException("Email already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode( request.getPassword()));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setCountry(request.getCountry());
        user.setJoinDate(LocalDateTime.now());
        user.setActive(request.isActive());
        user.setNotLocked(request.isNonLocked());
        user.setRole(request.getRole());
        user.setAuthorities(getRoleEnumName(request.getRole()).getAuthorities());
        user.setUserParent(userByParentUser.get());
        userRepository.save(user);

        return new HttpResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase().toUpperCase().toString(),"Usuario creado exitosamente!!");

    }

    @Override
    public HttpResponse updateUser(RegisterRequest request) throws UserNameExistsException, EmailExistsException, EmailNotFoundException, UserNameNotFoundException {

        return null;
    }

    @Override
    public HttpResponse registerUserSuperAdmin(RegisterRequest request) throws EmailNotFoundException, UserNameNotFoundException {

        Optional<User> userByUsername = userRepository.findByUsername(request.getUsername());
        if(userByUsername.isPresent()) {
            throw new UserNameNotFoundException("Already username exists");
        }

        Optional<User> userbyEmail = userRepository.findByEmail(request.getEmail());
        if(userbyEmail.isPresent()) {
            throw new EmailNotFoundException("Already email exists");
        }

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
        user.setRole(Role.ROLE_SUPER_ADMIN.toString());
        user.setAuthorities(Role.ROLE_SUPER_ADMIN.getAuthorities());
        userRepository.save(user);

        return new HttpResponse(HttpStatus.OK.value(), HttpStatus.OK, HttpStatus.OK.getReasonPhrase().toUpperCase(),"Usuario registrado exitosamente!!");
    }

    @Override
    public List<User> findByUserParent(Long userParentId) {

        LOGGER.info("FIND BY USER PARENT ID");
        User usuarioActual = authService.getUserAuthenticated();

        if (!usuarioActual.getRole().equals("ROLE_SUPER_ADMIN") || !usuarioActual.getId().equals(userParentId))
            throw new AccessDeniedException("No tienes permiso para acceder a esta información.");

        return userRepository.findByUserParentId(userParentId);
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
