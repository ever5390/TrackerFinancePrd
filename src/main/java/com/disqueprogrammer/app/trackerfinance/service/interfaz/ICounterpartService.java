package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Counterpart;

import java.util.List;

public interface ICounterpartService {
    Counterpart save(Counterpart counterpart) throws ObjectExistsException, ObjectNotFoundException;

    Counterpart findByIdAndWorkspaceId(Long counterpartId, Long workspaceId) throws ObjectNotFoundException;
    List<Counterpart> findByWorkspaceId(Long workspaceId);

    Counterpart update(Counterpart counterpart, Long counterpartId) throws ObjectExistsException, ObjectNotFoundException;

    void delete(Long counterpartId, Long workspaceId) throws ObjectExistsException, ObjectNotFoundException;
}
