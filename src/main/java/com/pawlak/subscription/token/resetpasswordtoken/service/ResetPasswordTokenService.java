package com.pawlak.subscription.token.resetpasswordtoken.service;

import com.pawlak.subscription.token.resetpasswordtoken.model.ResetPasswordToken;
import com.pawlak.subscription.token.resetpasswordtoken.repository.ResetPasswordTokenRepository;
import com.pawlak.subscription.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    public void createToken(User user) {

        String token = UUID.randomUUID().toString();

        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user,token);

        resetPasswordTokenRepository.save(resetPasswordToken);
    }
}
