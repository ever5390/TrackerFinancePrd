package com.disqueprogrammer.app.trackerfinance.security.dtoAuth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;
    private String country;
    private String role;
    private boolean isActive;
    private boolean isNonLocked;
}