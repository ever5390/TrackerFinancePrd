package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.CardType;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.CardTypeRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.CardTypeService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class CardTypeImpl implements CardTypeService {

    private CardTypeRepository cardTypeRepository;
    @Override
    public List<CardType> findAllByWorkspaceId(Long workspaceId) {
        return cardTypeRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public CardType findCardTypeByIdAndWorkspaceId(Long workspaceId, Long cardTypeId) {
        return cardTypeRepository.findByIdAndWorkspaceId(cardTypeId, workspaceId);
    }

    @Override
    public CardType create(CardType cardTypeRequest) throws CustomException {
        cardTypeRequest.setFixedParameter(false);
        CardType cardTypeByName = cardTypeRepository.findByNameAndWorkspaceId(cardTypeRequest.getName(), cardTypeRequest.getWorkspaceId());
        if(cardTypeByName != null)
            throw new CustomException("El nombre ingresado ya existe, por favor ingrese otro.");

        return cardTypeRepository.save(cardTypeRequest);
    }

    @Override
    public CardType update(CardType cardTypeRequest, Long cardTypeId) throws CustomException {
        CardType cardTypeFound = cardTypeRepository.findByIdAndWorkspaceId(cardTypeId, cardTypeRequest.getWorkspaceId());
        if(cardTypeFound == null )
            throw new CustomException("El elemento a modificar no ha sido encontrado.");

        if(cardTypeFound.isFixedParameter())
            throw new CustomException("El tipo de tarjeta " + cardTypeFound.getName() + " no puede ser editada ya que es la inicial y se requiere para distintos procedimientos.");


        if(!Objects.equals(cardTypeFound.getId(), cardTypeId))
            throw new CustomException("El nombre ingresado ya existe, por favor ingrese otro.");
        return cardTypeRepository.save(cardTypeRequest);
    }

    @Override
    public void delete(Long cardTypeId, Long workspaceId) throws CustomException {
        CardType cardTypeFound = cardTypeRepository.findByIdAndWorkspaceId(cardTypeId, workspaceId);
        if(cardTypeFound == null )
            throw new CustomException("El elemento a modificar no ha sido encontrado.");

        if(cardTypeFound.isFixedParameter())
            throw new CustomException("El tipo de tarjeta " + cardTypeFound.getName() + " no puede ser borrar ya que es la inicial y se requiere para distintos procedimientos.");

        cardTypeRepository.deleteById(cardTypeId);
    }
}
