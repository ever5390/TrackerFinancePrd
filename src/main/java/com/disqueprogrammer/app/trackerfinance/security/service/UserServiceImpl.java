package com.disqueprogrammer.app.trackerfinance.security.service;

import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.EmailExistsException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.EmailNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNameExistsException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNameNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.persistence.Role;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.persistence.UserRepository;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;

    private UserRepository userRepository;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public List<User> showUsers() {
        return this.userRepository.findAll();
    }

    public HttpResponse registerNewUser(RegisterRequest request) throws UserNameExistsException, EmailExistsException {

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
        userRepository.save(user);

        return new HttpResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase().toUpperCase().toString(),"Usuario creado exitosamente!!");

    }

    @Override
    public HttpResponse updateUser(RegisterRequest request) throws EmailNotFoundException, UserNameNotFoundException {
        Optional<User> userByUsername = userRepository.findByUsername(request.getUsername());
        if(!userByUsername.isPresent()) {
            throw new UserNameNotFoundException("Username not exists");
        }

        Optional<User> userbyEmail = userRepository.findByEmail(request.getEmail());
        if(!userbyEmail.isPresent()) {
            throw new EmailNotFoundException("Username not exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setCountry(request.getCountry());
        user.setActive(request.isActive());
        user.setNotLocked(request.isNonLocked());
        user.setRole(request.getRole());
        user.setAuthorities(getRoleEnumName(request.getRole()).getAuthorities());
        userRepository.save(user);

        return new HttpResponse(HttpStatus.OK.value(), HttpStatus.OK, HttpStatus.OK.getReasonPhrase().toUpperCase().toString(),"Usuario actualizado exitosamente!!");

    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
