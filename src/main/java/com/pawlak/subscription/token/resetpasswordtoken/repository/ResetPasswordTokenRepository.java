package com.pawlak.subscription.token.resetpasswordtoken.repository;

import com.pawlak.subscription.token.resetpasswordtoken.model.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {
    Optional<ResetPasswordToken> findByToken(String token);
}
