package com.disqueprogrammer.app.trackerfinance.security.service;

import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.EmailExistsException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.EmailNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNameExistsException;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNameNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.HttpResponse;
import com.disqueprogrammer.app.trackerfinance.security.dtoAuth.RegisterRequest;

import java.util.List;

public interface UserService {

    List<User> showUsers();

    HttpResponse registerNewUser(RegisterRequest request) throws UserNameExistsException, EmailExistsException;

    HttpResponse updateUser(RegisterRequest request) throws UserNameExistsException, EmailExistsException, EmailNotFoundException, UserNameNotFoundException;

}