package com.disqueprogrammer.app.trackerfinance.service.interfaz;

import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.CardType;

import java.util.List;

public interface CardTypeService {

    List<CardType> findAllByWorkspaceId(Long workspaceId);

    CardType findCardTypeByIdAndWorkspaceId(Long workspaceId, Long cardTypeId);

    CardType create(CardType cardTypeRequest) throws CustomException;

    CardType update(CardType cardTypeRequest, Long cardTypeId) throws CustomException;

    void delete(Long cardTypeId, Long workspaceId) throws CustomException;
}
