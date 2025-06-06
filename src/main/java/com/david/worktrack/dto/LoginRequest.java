package com.david.worktrack.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
