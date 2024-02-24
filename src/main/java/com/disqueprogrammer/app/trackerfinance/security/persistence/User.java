package com.disqueprogrammer.app.trackerfinance.security.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Getter
@Setter
@Entity
@Table(name="user", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email"})})
public class User implements UserDetails {

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

    private LocalDateTime lastLoginDate;

    private LocalDateTime lastLoginDateDisplay;

    private LocalDateTime joinDate;

    private String[] authorities;

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


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return stream(authorities).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isNotLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }



}
