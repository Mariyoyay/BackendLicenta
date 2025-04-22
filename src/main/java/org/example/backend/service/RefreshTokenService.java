package org.example.backend.service;

import org.example.backend.model.RefreshToken;
import org.example.backend.model.User;
import org.example.backend.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void deleteExpiredTokens() {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        refreshTokenRepository.deleteExpiredTokens(new java.sql.Date(yesterday.getTime()));
    }

    public void addNewRefreshToken(String refreshToken, Date expirationDate, User user) {
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setRefreshToken(refreshToken);
        newRefreshToken.setExpiryDate(new java.sql.Date(expirationDate.getTime()));
        newRefreshToken.setUser(user);
        refreshTokenRepository.save(newRefreshToken);
    }
}
