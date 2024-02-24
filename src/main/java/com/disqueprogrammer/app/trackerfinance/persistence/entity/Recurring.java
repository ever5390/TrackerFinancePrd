package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.PeriodEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Recurring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Enumerated(EnumType.STRING)
    private PeriodEnum period;

    private int numberOfTimes;

    private int dayMonth;

    private List<Integer> itemDateSelectedPerPeriod;

    private boolean statusIsPayed;

    private LocalDateTime nextClosestPaymentDate;

    private LocalDateTime nextPaymentDate;
}
