package com.example.MagicScreenBackend.Security;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}