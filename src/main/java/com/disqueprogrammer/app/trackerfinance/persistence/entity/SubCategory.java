package com.disqueprogrammer.app.trackerfinance.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String color;

    private String icon;

    private boolean used;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    private boolean active;

    private Long workspaceId;
}
