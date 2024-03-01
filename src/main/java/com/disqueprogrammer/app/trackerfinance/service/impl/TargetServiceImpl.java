package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Tag;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TagRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TargetServiceImpl implements TagService {

    TagRepository tagRepository;
    public TargetServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag save(Tag tag) throws CustomException {
        validateDuplicatedName(tag);
        return tagRepository.save(tag);
    }

    private void validateDuplicatedName(Tag tag) throws CustomException {
        Tag tagFounded = tagRepository.findByNameAndWorkspaceId(tag.getName(), tag.getWorkspaceId());
        if(tagFounded != null)
            throw new CustomException("Ya existe un registro con el nombre indicado, ingrese otro.");
    }

    @Override
    public Tag findByIdAndWorkspaceId(Long tagId, Long workspaceId) throws CustomException {
        return tagRepository.findByIdAndWorkspaceId(tagId, workspaceId);
    }

    @Override
    public List<Tag> findByWorkspaceId(Long workspaceId) {
        return tagRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public Tag update(Tag tag, Long tagId) throws CustomException {
        Tag tagFounded = tagRepository.findByIdAndWorkspaceId(tagId, tag.getWorkspaceId());
        if(tagFounded != null && !tagFounded.getName().equalsIgnoreCase(tag.getName()))
            throw new CustomException("Ya existe un registro con el nombre que intentas actualizar, ingrese otro.");
        return tagRepository.save(tag);
    }

    @Override
    public void delete(Long tagId, Long workspaceId) throws CustomException {
        Tag tagFounded = tagRepository.findByIdAndWorkspaceId(tagId, workspaceId);
        if(tagFounded == null)
            throw new CustomException("El registro que intentas eliminar no existe.");
        tagRepository.deleteById(tagId);
    }
}
