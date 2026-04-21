package com.david.worktrack.service;

import com.david.worktrack.entity.AppUser;
import com.david.worktrack.service.token.ConfirmationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {

    public ConfirmationToken createToken(AppUser user) {
        return ConfirmationToken.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

    }
}
