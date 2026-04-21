package com.david.worktrack.refreshToken;

import com.david.worktrack.entity.AppUser;
import com.david.worktrack.exception.InvalidTokenException;
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
    public String createRefreshToken(AppUser appUser) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAppUser(appUser);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setUsed(false);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    // Check Refresh Token
    public RefreshToken validateRefreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = getRefreshTokenOrThrow(refreshTokenValue);

        if (refreshToken.isUsed()) {
            throw new InvalidTokenException("Refresh token already used");
        }
        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new InvalidTokenException("Expired refresh token");
        }
        return refreshToken;
    }

    // Log Out Invalidate Refresh Token
    @Transactional
    public void invalidateRefreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = getRefreshTokenOrThrow(refreshTokenValue);

        refreshToken.setUsed(true);
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken getRefreshTokenOrThrow(String refreshTokenValue) {
        return refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
    }
}
