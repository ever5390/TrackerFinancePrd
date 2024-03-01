package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String detail;

    private double limitAmount;

    private double usedAmount;

    private LocalDateTime dateBegin;

    private LocalDateTime dateEnd;

    private boolean statusOpen;

    private boolean hasAutomaticRegeneration;

    @OneToMany
    private List<SubCategory> subCategories;

    @OneToMany
    private List<Transaction> transactions;

    private String code;

    private Long workspaceId;

}
