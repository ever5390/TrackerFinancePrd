package com.disqueprogrammer.app.trackerfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeMovementDto {

    private int totalNumberElements;

    private double totalIN;

    private double totalOUT;

    private List<MovementDto> movememts;
}
