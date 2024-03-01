package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Workspace;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNotFoundException;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface WorkspaceService {

    void validationWorkspaceUserRelationship(Long workspaceId) throws CustomException;

    User getUserAuhenticated();

    Workspace createWorkspace(Workspace workspaceReq) throws UserNotFoundException, CustomException;

    Workspace associateUsersToWorkspace(Workspace workspace) throws CustomException, UserNotFoundException;

    void deleteById(Long workspaceId, Long id) throws CustomException;

    List<Workspace> findAllByUserId(Long userId) throws UserNotFoundException;


}
