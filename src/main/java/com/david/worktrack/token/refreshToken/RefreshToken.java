package com.david.worktrack.token.refreshToken;

import com.david.worktrack.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private AppUser appUser;

    private String token;

    private LocalDateTime expiryDate;

    private boolean used;
}
