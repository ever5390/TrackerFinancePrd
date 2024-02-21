package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El campo monto no puede ser nulo")
    @NotEmpty(message = "El campo monto no puede ser vacío")
    private double amount;

    private String description;

    //@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    private TypeEnum type;

    @NotNull(message = "El campo medio de pago origen no puede ser nulo")
    @NotEmpty(message = "El campo medio de pago origen no puede ser vacío")
    @ManyToOne
    private PaymentMethod paymentMethod;

    @NotNull(message = "El campo medio de pago destino no puede ser nulo")
    @NotEmpty(message = "El campo medio de pago destino no puede ser vacío")
    @ManyToOne
    private PaymentMethod paymentMethodDestiny;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @Enumerated(EnumType.STRING)
    private ActionEnum action;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    private BlockEnum block;

    private double remaining;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    private Long idLoanAssoc;

    private Long userId;

}
