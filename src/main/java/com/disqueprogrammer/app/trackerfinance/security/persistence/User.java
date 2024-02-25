package com.disqueprogrammer.app.trackerfinance.security.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="user", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email"})})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @NotBlank(message = "username field cannot be empty")
    private String username;

    @Basic
    @NotBlank(message = "email field cannot be empty")
    private String email;
    private String lastname;

    @NotBlank(message = "firstname field cannot be empty")
    private String firstname;

    private String country;

    private String password;

    private String role;

    private String[] authorities;

    private LocalDateTime lastLoginDate;

    private LocalDateTime lastLoginDateDisplay;

    private LocalDateTime joinDate;

    private boolean isActive;

    private boolean isNotLocked;

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setNotLocked(boolean notLocked) {
        isNotLocked = notLocked;
    }

    @ManyToOne
    @JoinColumn(name = "user_parent_id")
    private User userParent;

    public User() {}

}
