package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Account;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.CardType;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.AccountRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.CardTypeRepository;
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

import java.math.BigDecimal;
import java.util.*;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final WorkspaceRepository workspaceRepository;

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final CardTypeRepository cardTypeRepository;

    private final AuthService authService;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository, UserRepository userRepository, AccountRepository accountRepository, CardTypeRepository cardTypeRepository, AuthService authService) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.authService = authService;
    }

    @Override
    public void validationWorkspaceUserRelationship(Long workspaceId) throws CustomException {
        User userAuthenticated = authService.getUserAuthenticated();
        Optional<Workspace> workspaceFound = workspaceRepository.findById(workspaceId);
        if(workspaceFound.isEmpty())
            throw new CustomException("No se encontró este espacio de trabajo seleccionado.");

        if(!workspaceFound.get().getUsers().stream().anyMatch(user -> user.getId().equals(userAuthenticated.getId())))
            throw new CustomException("No tienes permisos para este recurso.");
    }

    @Override
    public User getUserAuhenticated() {
        return authService.getUserAuthenticated();
    }

    @Override
    public Workspace createWorkspace(Workspace workspaceReq) throws UserNotFoundException, CustomException {

        if(workspaceReq.getOwner() == null || workspaceReq.getOwner().getId() == 0)
            throw new CustomException("El propietario es requerido.");

        // user validated exist
        User userParent = userRepository.findById(Math.toIntExact(workspaceReq.getOwner().getId())).orElseThrow(()-> new UserNotFoundException("El usuario no ha sido encontrado"));
        // user role == super_admin
        if(!userParent.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No tienes permisos para efectuar esta operación.");
        // workspace name exists
        Workspace workspaceFound = workspaceRepository.findByName(workspaceReq.getName());
        if(workspaceFound != null)  throw new CustomException("Ya existe un espacio de trabajo con el mismo nombre, ingrese otro.");

        workspaceReq.setActive(true);
        //Associate user with worskpace
        workspaceReq.setOwner(userParent);
        workspaceReq.getUsers().add(userParent);

        Workspace workspaceSaved = workspaceRepository.save(workspaceReq);

        Account accountBegin = new Account();
        accountBegin.setName("EFECTIVO");
        accountBegin.setWorkspaceId(workspaceSaved.getId());
        accountBegin.setCurrentBalance(BigDecimal.ONE);
        accountBegin.setBeginBalance(BigDecimal.ONE);
        accountBegin.setActive(true);
//        accountBegin.setPaymentMethods(null);
//        accountBegin.setCardType(null);
        accountRepository.save(accountBegin);

        CardType cardTypeBegin = new CardType();
        cardTypeBegin.setName("CREDITO");
        cardTypeBegin.setFixedParameter(true);
        cardTypeBegin.setWorkspaceId(workspaceSaved.getId());
        cardTypeRepository.save(cardTypeBegin);

        return workspaceSaved;
    }

    @Override
    public Workspace associateUsersToWorkspace(Workspace workspaceReq) throws CustomException, UserNotFoundException {
        /*
        // userParent exists
        User userParent = userRepository.findById(Math.toIntExact(userParentId)).orElseThrow(()-> new UserNotFoundException("El código del usuario administrador no sido encontrado"));

        // userParent has role super_admin // hasRole('ROLE_SUPER_ADMIN')
        if(!userParent.getRole().equals(Role.ROLE_SUPER_ADMIN.toString())) throw new CustomException("No tienes permisos para efectuar esta operación.");
        */

        if(workspaceReq.getOwner() == null || workspaceReq.getOwner().getId() == 0)
            throw new CustomException("El propietario es requerido.");

        // Workspace exists
        Workspace workspaceFound = workspaceRepository.findById(workspaceReq.getId()).orElseThrow(()-> new CustomException("El espacio de trabajo no ha sido encontrado"));

        // workspace name exists
        Workspace workspaceFoundByName= workspaceRepository.findByName(workspaceReq.getName());
        if(workspaceFoundByName != null && !Objects.equals(workspaceFoundByName.getId(), workspaceFound.getId()))  throw new CustomException("Ya existe un espacio de trabajo con el mismo nombre, ingrese otro.");

        User userAuthenticated = authService.getUserAuthenticated();

        if(!Objects.equals(workspaceFound.getOwner().getId(), userAuthenticated.getId())) {
            throw new CustomException("No cuentas con permisos para realizar esta operación, comunícaselo a tu administrador.");
        }

        if (!workspaceReq.getUsers().contains(workspaceFound.getOwner())) {
            workspaceReq.getUsers().add(workspaceFound.getOwner());
        }

        return workspaceRepository.save(workspaceReq);
    }


    @Override
    public List<Workspace> findAllByUserId(Long userId) throws UserNotFoundException {
        User user = userRepository.findById(Math.toIntExact(userId)).orElseThrow(()-> new UserNotFoundException("El código del usuario no sido encontrado"));
        return workspaceRepository.findWorkspacesByUserId(userId);
    }

    @Override
    public void deleteById(Long workspaceId, Long id) throws CustomException {
        // Workspace exists
        Workspace workspaceFound = workspaceRepository.findById(workspaceId).orElseThrow(()-> new CustomException("El espacio de trabajo no ha sido encontrado"));
        if(!Objects.equals(workspaceFound.getOwner().getId(), id)) throw new CustomException("No cuentas con permisos para realizar esta operación, comunícaselo a tu administrador.");
        workspaceRepository.deleteById(workspaceId);
    }

}
