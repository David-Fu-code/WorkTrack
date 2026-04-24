package com.david.worktrack.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String displayName;
    private String email;
    private String password;
}