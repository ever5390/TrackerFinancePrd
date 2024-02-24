package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Tag;

import java.util.List;

public interface TagService {

    Tag save(Tag tag) throws CustomException;

    Tag findByIdAndWorkspaceId(Long tagId, Long workspaceId) throws CustomException;

    List<Tag> findByWorkspaceId(Long workspaceId);

    Tag update(Tag tag, Long tagId) throws CustomException;

    void delete(Long tagId, Long workspaceId) throws CustomException;
    
}
