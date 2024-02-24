package com.disqueprogrammer.app.trackerfinance.security.service;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.*;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.RegisterRequest;

import java.util.List;

public interface UserService {

    List<User> showUsers();

    HttpResponse registerNewUser(Long userParentId, RegisterRequest request) throws UserNameExistsException, EmailExistsException, CustomException;

    HttpResponse updateUser(RegisterRequest request) throws UserNameExistsException, EmailExistsException, EmailNotFoundException, UserNameNotFoundException;

    HttpResponse registerUserSuperAdmin(RegisterRequest request) throws EmailNotFoundException, UserNameNotFoundException;

    List<User> findByUserParent(Long userParentId) throws UserNotFoundException;
}