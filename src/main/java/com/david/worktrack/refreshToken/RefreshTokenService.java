package com.david.worktrack.refreshToken;

import com.david.worktrack.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // New Refresh Token to keep log in for 7 days
    public String createRefreshToken(AppUser user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAppUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setUsed(false);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    // Check Refresh Token
    public RefreshToken validateRefreshToken(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        if (refreshToken.isUsed()) {
            throw new IllegalStateException("Refresh token already used");
        }
        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Expired refresh token");
        }
        return refreshToken;
    }

    // Log Out Invalidate Refresh Token
    @Transactional
    public void invalidateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        token.setUsed(true);
        refreshTokenRepository.save(token);
    }
}
