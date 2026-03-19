package com.pawlak.subscription.token.resetpasswordtoken.repository;

import com.pawlak.subscription.token.resetpasswordtoken.model.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {
    Optional<ResetPasswordToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM ResetPasswordToken t WHERE t.user.id IN :userIds")
    void deleteAllByUserIds(@Param("userIds") List<UUID> userIds);
}
