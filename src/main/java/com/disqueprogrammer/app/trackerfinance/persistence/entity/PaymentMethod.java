package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String icon;

    private String color;

    private boolean used;

    private boolean active;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = true)
    //@JsonIgnoreProperties("paymentMethods")
    private Account account;

    private Long workspaceId;

}
