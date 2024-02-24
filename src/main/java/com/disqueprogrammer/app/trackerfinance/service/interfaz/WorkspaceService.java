package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Workspace;
import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNotFoundException;

import java.util.List;

public interface WorkspaceService {

    void validationWorkspaceUserRelationship(Long workspaceId) throws CustomException;

    Workspace save(Workspace workspaceReq, Long userParentId) throws UserNotFoundException, CustomException;
    Workspace saveAssocOneUserWorkspaceRelationship(Long userParentId, Long userAssocId, Long workspaceId) throws CustomException, UserNotFoundException;

    Workspace saveAssocUsersWorkspaceRelationship(Long userParentId, Long workspaceId, Workspace workspace) throws CustomException, UserNotFoundException;

    List<Workspace> findAllByUserId(Long userParentId) throws UserNotFoundException;

    void deleteById(Long workspaceId) throws CustomException;

}
