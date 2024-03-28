package com.disqueprogrammer.app.trackerfinance.service.interfaz.transaction;

import com.disqueprogrammer.app.trackerfinance.dto.FiltersDTO;
import com.disqueprogrammer.app.trackerfinance.dto.ResumeMovementDto;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;

public interface ITransactionFiltersService {

    FiltersDTO filterReload(Long workspaceId);

    ResumeMovementDto findMovementsByFilters2(Long workspaceIdParam, FiltersDTO filtersDTO) throws Exception;
}
