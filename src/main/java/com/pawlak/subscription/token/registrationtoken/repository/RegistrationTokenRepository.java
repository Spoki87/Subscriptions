package com.pawlak.subscription.token.registrationtoken.repository;

import com.pawlak.subscription.token.registrationtoken.model.RegistrationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, UUID> {
    Optional<RegistrationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE RegistrationToken c SET c.confirmedTime = :confirmationTime WHERE c.token = :token")
    int updateConfirmedTime(String token, LocalDateTime confirmationTime);
}
