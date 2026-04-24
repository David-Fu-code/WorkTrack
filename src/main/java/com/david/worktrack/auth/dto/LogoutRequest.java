package com.david.worktrack.auth.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
