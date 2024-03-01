package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    private String description;

    //@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    private TypeEnum type;

    @ManyToOne
    private Account account;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = true)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "account_destiny_id", nullable = true)
    private Account accountDestiny;

    @ManyToOne
    @JoinColumn(name = "payment_method_destiny_id", nullable = true)
    private PaymentMethod paymentMethodDestiny;

    @ManyToOne
    @JoinColumn(name = "subCategory_id")
    private SubCategory subCategory;

    @Enumerated(EnumType.STRING)
    private ActionEnum action;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    private BlockEnum block;

    private double remaining;

    @ManyToMany
    @JoinTable(name = "tag_transaction", joinColumns = @JoinColumn(name = "fk_transaction"), inverseJoinColumns = @JoinColumn(name = "fk_tag"))
    private List<Tag> tags;

    @ManyToOne
    @JoinColumn(name = "counterpart_id", nullable = true)
    private Counterpart counterpart;

    @OneToOne
    @JoinColumn(name = "recurring_id", nullable = true)
    private Recurring recurring;

    private Long idLoanAssoc;

    private Long userId;

    private Long workspaceId;

}
