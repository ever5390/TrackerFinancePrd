package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;

import java.util.List;

public interface ITransactionFiltersService {

    ResumeMovementDto findMovementsByFilters(Long userId, String startDate, String endDate, TypeEnum type, StatusEnum status, String category, String description, String segment, String account, String paymentMethod, BlockEnum block, ActionEnum action) throws Exception;
}
