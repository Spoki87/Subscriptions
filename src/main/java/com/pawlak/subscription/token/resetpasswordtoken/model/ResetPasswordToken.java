package com.pawlak.subscription.token.resetpasswordtoken.model;

import com.pawlak.subscription.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class ResetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    private LocalDateTime createdTime;

    private LocalDateTime expiredTime;

    @ManyToOne
    @JoinColumn(nullable = false, name = "app_user_id")
    private User user;

    public ResetPasswordToken(User user, String token){
        this.createdTime = LocalDateTime.now();
        this.expiredTime = LocalDateTime.now().plusHours(12);
        this.token = token;
        this.user = user;
    }
}
