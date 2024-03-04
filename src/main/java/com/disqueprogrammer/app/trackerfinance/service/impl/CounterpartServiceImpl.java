package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Counterpart;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.CounterpartRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.ICounterpartService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CounterpartServiceImpl implements ICounterpartService {

    private final static Logger LOG = LoggerFactory.getLogger(CounterpartServiceImpl.class);

    private final CounterpartRepository counterpartRepository;

    private final TransactionRepository transactionRepository;

    public CounterpartServiceImpl(CounterpartRepository counterpartRepository, TransactionRepository transactionRepository) {
        this.counterpartRepository = counterpartRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Counterpart save(Counterpart counterpartRequest) throws ObjectExistsException, ObjectNotFoundException, CustomException {
        validNameCounterpart(counterpartRequest.getName(), counterpartRequest.getWorkspaceId());
        if(!StringUtils.isEmpty(counterpartRequest.getEmail())) {
            validEmailCounterpart(counterpartRequest.getEmail(), counterpartRequest.getWorkspaceId());
        }
        return counterpartRepository.save(counterpartRequest);
    }

    private void validNameCounterpart(String name, Long workspaceId) throws CustomException {
        if(StringUtils.isEmpty(name)) throw new CustomException("El campo nombre es requerido");

        Counterpart counterpartFoundedByName = counterpartRepository.findByNameAndWorkspaceId(name, workspaceId);
        if (counterpartFoundedByName != null) throw new CustomException("Ya existe un miembro con el nombre: " + name + ", ingrese otro");
    }

    private void validEmailCounterpart(String email, Long workspaceId) throws CustomException {
        boolean hasValidFormatEmail = validEmailFormat(email);
        if(!hasValidFormatEmail)
            throw new CustomException("El email ingresado no tiene un formato correcto");

        Counterpart counterpartFoundedByEmail = counterpartRepository.findByEmailAndWorkspaceId(email, workspaceId);
        if (counterpartFoundedByEmail != null) throw new CustomException("Ya existe un miembro con el email: " + email + ", ingrese otro");
    }

    public boolean validEmailFormat(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public Counterpart findByIdAndWorkspaceId(Long counterpartId, Long workspaceId) throws ObjectNotFoundException {

        Counterpart counterpartFounded = counterpartRepository.findByIdAndWorkspaceId(counterpartId, workspaceId);
        if(counterpartFounded == null)
            throw  new ObjectNotFoundException("No se encontró al miembro seleccionado");

        return counterpartFounded;
    }

    @Override
    public List<Counterpart> findByWorkspaceId(Long workspaceId) {
        return counterpartRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public Counterpart update(Counterpart counterpartRequest, Long counterpartId) throws ObjectExistsException, ObjectNotFoundException, CustomException {

        Counterpart counterpartFounded = counterpartRepository.findByIdAndWorkspaceId(counterpartId, counterpartRequest.getWorkspaceId());

        if(counterpartFounded == null)
            throw  new ObjectNotFoundException("No se encontró al miembro seleccionado");

        if(!counterpartRequest.getName().equalsIgnoreCase(counterpartFounded.getName())) {
            validNameCounterpart(counterpartRequest.getName(), counterpartRequest.getWorkspaceId());
        }

        if(!counterpartFounded.getEmail().equalsIgnoreCase(counterpartRequest.getEmail())) {
            validEmailCounterpart(counterpartRequest.getEmail(), counterpartRequest.getWorkspaceId());
        }

        counterpartFounded.setName(counterpartRequest.getName().toLowerCase());
        counterpartFounded.setEmail(counterpartRequest.getEmail());

        return counterpartRepository.save(counterpartFounded);
    }

    @Override
    public void delete(Long counterpartId, Long workspaceId) throws ObjectNotFoundException {

        Counterpart counterpartFounded = counterpartRepository.findByIdAndWorkspaceId(counterpartId, workspaceId);
        if (counterpartFounded == null)
            throw new ObjectNotFoundException("No se encontró al usuario seleccionado");

        counterpartRepository.deleteById(counterpartId);
    }

    private void validateParamas(Counterpart counterpartRequest) throws ObjectExistsException, ObjectNotFoundException {

        if(StringUtils.isEmpty(counterpartRequest.getName()) || counterpartRequest.getName() == null){
            throw new ObjectNotFoundException("El campo nombre no puede ser vacío o nulo");
        }

        if(StringUtils.isEmpty(counterpartRequest.getEmail()) || counterpartRequest.getEmail() == null){
            Counterpart counterpartNameRepeated = counterpartRepository.findByEmailAndWorkspaceId(counterpartRequest.getEmail(), counterpartRequest.getWorkspaceId());
            if(counterpartNameRepeated != null) {
                throw new ObjectExistsException("Ya existe un miembro con el correo que intentas registrar");
            }
        }
    }

}
