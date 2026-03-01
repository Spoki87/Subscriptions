package com.pawlak.subscription.security.refresh;

import com.pawlak.subscription.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String tokenHash;
    private LocalDateTime expiryDate;
    private LocalDateTime sessionExpiryDate;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private User user;

    private boolean revoked = false;

    public RefreshToken(String tokenHash, LocalDateTime expiryDate, LocalDateTime sessionExpiryDate, User user) {
        this.tokenHash = tokenHash;
        this.expiryDate = expiryDate;
        this.sessionExpiryDate = sessionExpiryDate;
        this.user = user;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    public void revoke() {
        this.revoked = true;
    }

}

