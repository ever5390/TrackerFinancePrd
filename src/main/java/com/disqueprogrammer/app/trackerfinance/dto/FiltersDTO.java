package com.disqueprogrammer.app.trackerfinance.dto;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FiltersDTO {
    private Long workspaceIdParam;
    private String startDate;
    private String endDate;
    private LocalDateTime startDateLDT;
    private LocalDateTime endDateLDT;
    private String description;
    private BlockEnum block;
    private ListItemFilter categories;
    private ListItemFilter subcategories;
    private ListItemFilter accounts;
}
