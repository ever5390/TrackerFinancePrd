package com.disqueprogrammer.app.trackerfinance.dto;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeMovementDto {

    private BigDecimal totalIN;

    private BigDecimal totalOUT;

    private BigDecimal totalTheyOweMe;

    private BigDecimal totalIOweYou;

    private List<Transaction> movememts;
}
