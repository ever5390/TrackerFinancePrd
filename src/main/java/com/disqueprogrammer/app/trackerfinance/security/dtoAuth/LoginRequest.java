package com.disqueprogrammer.app.trackerfinance.security.dtoAuth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class LoginRequest {
    private String username;
    private String password;
}
