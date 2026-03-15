package com.pawlak.subscription.security.refresh;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRateString = "${cleanup.refresh-token.interval-ms:3600000}")
    @Transactional
    public void removeExpiredAndRevokedTokens() {
        refreshTokenRepository.deleteAllExpiredOrRevoked(LocalDateTime.now());
    }
}
