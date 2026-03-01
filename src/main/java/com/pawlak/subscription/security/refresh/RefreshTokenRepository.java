package com.pawlak.subscription.security.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String hash);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.revoked = true OR t.expiryDate < :now OR t.sessionExpiryDate < :now")
    int deleteAllExpiredOrRevoked(@Param("now") LocalDateTime now);
}
