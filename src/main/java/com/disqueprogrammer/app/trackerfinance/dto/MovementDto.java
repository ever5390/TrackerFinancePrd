package com.disqueprogrammer.app.trackerfinance.dto;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovementDto {
    private String description;

    private String headerTitle;

    private String status;

    private String amount;

    private TypeEnum type;

    private ActionEnum action;

    private String category;

    private String paymentMethod;

    private String segment;

    private LocalDateTime createAt;

    private Long idTransactionAssoc;
}
