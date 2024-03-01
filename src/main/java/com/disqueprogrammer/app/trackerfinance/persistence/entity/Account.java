package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name="accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String icon;

    private String color;

    private double beginBalance;

    private double currentBalance;

    private boolean active;

    @OneToMany(mappedBy = "account")
    @JsonIgnoreProperties("account")
    @JsonProperty("paymentMethods")
    private List<PaymentMethod> paymentMethods;

    private Long workspaceId;
}
