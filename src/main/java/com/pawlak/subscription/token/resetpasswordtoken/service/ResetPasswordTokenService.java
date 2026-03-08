package com.pawlak.subscription.token.resetpasswordtoken.service;

import com.pawlak.subscription.email.EmailSender;
import com.pawlak.subscription.token.emailbuilder.TokenEmailTemplateBuilder;
import com.pawlak.subscription.token.resetpasswordtoken.model.ResetPasswordToken;
import com.pawlak.subscription.token.resetpasswordtoken.repository.ResetPasswordTokenRepository;
import com.pawlak.subscription.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final TokenEmailTemplateBuilder tokenEmailTemplateBuilder;
    private final EmailSender emailSender;

    public void createToken(User user) {

        String token = UUID.randomUUID().toString();

        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user,token);

        String html = tokenEmailTemplateBuilder.buildResetPasswordEmail(token);
        emailSender.send(user.getEmail(),html,"Reset password");

        resetPasswordTokenRepository.save(resetPasswordToken);
    }

    public void confirmToken(String token) {
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if(resetPasswordToken.getExpiredTime().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Token has expired");
        }
        resetPasswordTokenRepository.delete(resetPasswordToken);
    }
}
