package com.david.worktrack.refreshToken;

import com.david.worktrack.entity.AppUser;
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
