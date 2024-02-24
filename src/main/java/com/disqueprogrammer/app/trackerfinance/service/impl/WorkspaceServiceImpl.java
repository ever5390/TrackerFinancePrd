package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.security.Exceptions.domain.UserNotFoundException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Workspace;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.WorkspaceRepository;
import com.disqueprogrammer.app.trackerfinance.security.persistence.Role;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import com.disqueprogrammer.app.trackerfinance.security.persistence.UserRepository;
import com.disqueprogrammer.app.trackerfinance.security.service.AuthService;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final WorkspaceRepository workspaceRepository;

    private final UserRepository userRepository;

    private final AuthService authService;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository, UserRepository userRepository, AuthService authService) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void validationWorkspaceUserRelationship(Long workspaceId) throws CustomException {
        User userAuthenticated = authService.getUserAuthenticated();
        Optional<Workspace> workspaceFound = workspaceRepository.findById(workspaceId);
        if(workspaceFound.isEmpty())
            throw new CustomException("No se pudo procesar el espacio de trabajo en esta operación, actualice su navegador e inténtelo de nuevo.");

        if(!workspaceFound.get().getUsers().stream().anyMatch(user -> user.getId().equals(userAuthenticated.getId())))
            throw new CustomException("No tienes permisos para este recurso.");
    }

    @Override
    public Workspace save(Workspace workspaceReq, Long userParentId) throws UserNotFoundException, CustomException {
        // user validated exist
        User userParent = userRepository.findById(Math.toIntExact(userParentId)).orElseThrow(()-> new UserNotFoundException("El usuario no ha sido encontrado"));
        // user role == super_admin
        if(!userParent.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No tienes permisos para efectuar esta operación.");
        // workspace name exists
        Workspace workspaceFound = workspaceRepository.findByName(workspaceReq.getName());
        if(workspaceFound != null)  throw new CustomException("Ya existe un espacio de trabajo con el mismo nombre, ingrese otro.");
        //Associate user with worskpace
        workspaceReq.getUsers().add(userParent);

        return workspaceRepository.save(workspaceReq);
    }

    @Override
    public Workspace saveAssocOneUserWorkspaceRelationship(Long userParentId, Long userAssocId, Long workspaceId) throws CustomException, UserNotFoundException {
        // userParent exists
        User userParent = userRepository.findById(Math.toIntExact(userParentId)).orElseThrow(()-> new UserNotFoundException("El código del usuario administrador no sido encontrado"));

        // userParent has role super_admin // hasRole('ROLE_SUPER_ADMIN')
        if(!userParent.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No tienes permisos para efectuar esta operación.");

        // Workspace exists
        Workspace workspaceFound = workspaceRepository.findById(workspaceId).orElseThrow(()-> new CustomException("El espacio de trabajo no ha sido encontrado"));

        // UserParent has this workspace
        if(workspaceFound.getUsers().stream().noneMatch(user -> user.getId().equals(userParent.getId())))
            throw new CustomException("Este usuario no tiene permisos para efectuar cambios a este espacio de trabajo.");

        // userAssoc exists
        User userAssoc = userRepository.findById(Math.toIntExact(userAssocId)).orElseThrow(()-> new UserNotFoundException("El código del usuario que intentas asociar no ha sido encontrado"));

        // userAssoc dont has role super_admin
        if(userAssoc.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No puede haber más de un usuario padre en una tienda.");

        // userAssoc is new in the list workspace users
        if(workspaceFound.getUsers().stream().anyMatch(user -> user.getId().equals(userAssoc.getId())))
            throw new CustomException("El usuario seleccionado ya se encuentra asociado a este espacio de trabajo.");

        //Associate user with worskpace
        workspaceFound.getUsers().add(userAssoc);

        return workspaceRepository.save(workspaceFound);
    }

    @Override
    public Workspace saveAssocUsersWorkspaceRelationship(Long userParentId, Long workspaceId, Workspace workspaceReq) throws CustomException, UserNotFoundException {
        // userParent exists
        User userParent = userRepository.findById(Math.toIntExact(userParentId)).orElseThrow(()-> new UserNotFoundException("El código del usuario administrador no sido encontrado"));

        // userParent has role super_admin // hasRole('ROLE_SUPER_ADMIN')
        if(!userParent.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No tienes permisos para efectuar esta operación.");

        // Workspace exists
        Workspace workspaceFound = workspaceRepository.findById(workspaceId).orElseThrow(()-> new CustomException("El espacio de trabajo no ha sido encontrado"));

        // workspace name exists
        Workspace workspaceFoundByName= workspaceRepository.findByName(workspaceReq.getName());
        if(workspaceFoundByName != null && !Objects.equals(workspaceFoundByName.getId(), workspaceFound.getId()))  throw new CustomException("Ya existe un espacio de trabajo con el mismo nombre, ingrese otro.");

        // UserParent has this workspace
        if(workspaceFound.getUsers().stream().noneMatch(user -> user.getId().equals(userParent.getId())))
            throw new CustomException("Este usuario no tiene permisos para efectuar cambios a este espacio de trabajo.");

        // setters new users assoc to workspace
        workspaceFound.setUsers(workspaceReq.getUsers());

        return workspaceRepository.save(workspaceFound);
    }

    @Override
    public List<Workspace> findAllByUserId(Long userId) throws UserNotFoundException {
        User user = userRepository.findById(Math.toIntExact(userId)).orElseThrow(()-> new UserNotFoundException("El código del usuario no sido encontrado"));
        return workspaceRepository.findWorkspacesByUserId(userId);
    }

    @Override
    public void deleteById(Long workspaceId) throws CustomException {
        // Workspace exists
        Workspace workspaceFound = workspaceRepository.findById(workspaceId).orElseThrow(()-> new CustomException("El espacio de trabajo no ha sido encontrado"));
        workspaceRepository.deleteById(workspaceId);
    }
}
