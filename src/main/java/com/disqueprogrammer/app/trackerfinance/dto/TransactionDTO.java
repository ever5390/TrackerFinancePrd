package com.disqueprogrammer.app.trackerfinance.dto;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.*;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.ActionEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.BlockEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.StatusEnum;
import com.disqueprogrammer.app.trackerfinance.persistence.entity.enums.TypeEnum;
import com.disqueprogrammer.app.trackerfinance.security.persistence.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTO {

    private Long id;

    private double amount;

    private String description;

    private LocalDateTime createAt;

    private TypeEnum type;

    private Account account;

    private PaymentMethod paymentMethod;

    private Account accountDestiny;

    private PaymentMethod paymentMethodDestiny;

    private SubCategory subCategory;

    private ActionEnum action;

    private StatusEnum status;

    private BlockEnum block;

    private double remaining;

    private List<Tag> tags;

    private Counterpart counterpart;

    private Recurring recurring;

    private Transaction  loanAssocToPaymentRegister;

    private String detailLoanAssocToPaymentRegister;

    private Long idResponsableUser;
    
    private String nameResponsableUser;

    private Long workspaceId;
}
