package com.david.worktrack.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
