package com.pawlak.subscription.token.resetpasswordtoken.service;

import com.pawlak.subscription.email.EmailSender;
import com.pawlak.subscription.exception.domain.ResetPasswordTokenExpiredException;
import com.pawlak.subscription.exception.domain.ResetPasswordTokenNotFoundException;
import com.pawlak.subscription.exception.domain.UserNotFoundException;
import com.pawlak.subscription.token.emailbuilder.TokenEmailTemplateBuilder;
import com.pawlak.subscription.token.resetpasswordtoken.model.ResetPasswordToken;
import com.pawlak.subscription.token.resetpasswordtoken.repository.ResetPasswordTokenRepository;
import com.pawlak.subscription.user.model.User;
import com.pawlak.subscription.user.repository.UserRepository;
import com.pawlak.subscription.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    private final UserRepository userRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final TokenEmailTemplateBuilder tokenEmailTemplateBuilder;
    private final EmailSender emailSender;

    public void createToken(String email) {

        String token = UUID.randomUUID().toString();

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!user.isEnabled()) {
            throw new UserNotFoundException();
        }

        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user,token);

        String html = tokenEmailTemplateBuilder.buildResetPasswordEmail(token);
        emailSender.send(user.getEmail(),html,"Reset password");

        resetPasswordTokenRepository.save(resetPasswordToken);
    }

    @Transactional
    public void confirmToken(String token) {
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token)
                .orElseThrow(ResetPasswordTokenNotFoundException::new);
        if (resetPasswordToken.getExpiredTime().isBefore(LocalDateTime.now())) {
            resetPasswordTokenRepository.delete(resetPasswordToken);
            throw new ResetPasswordTokenExpiredException();
        }
        resetPasswordTokenRepository.delete(resetPasswordToken);
    }
}
