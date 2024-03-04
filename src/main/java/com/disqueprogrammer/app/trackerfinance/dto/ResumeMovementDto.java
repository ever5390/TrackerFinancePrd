package com.disqueprogrammer.app.trackerfinance.dto;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeMovementDto {

    private double totalIN;

    private double totalOUT;

    private double totalTheyOweMe;

    private double totalIOweYou;

    private List<Transaction> movememts;
}
