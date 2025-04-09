package org.example.backend.service;

import org.example.backend.repository.BlacklistedRefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BlacklistedTokensService {

    private final BlacklistedRefreshTokenRepository blacklistedRefreshTokenRepository;

    public BlacklistedTokensService(BlacklistedRefreshTokenRepository blacklistedRefreshTokenRepository) {
        this.blacklistedRefreshTokenRepository = blacklistedRefreshTokenRepository;
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void blacklistedRefreshTokens() {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        blacklistedRefreshTokenRepository.deleteExpiredTokens(new java.sql.Date(yesterday.getTime()));
    }
}
