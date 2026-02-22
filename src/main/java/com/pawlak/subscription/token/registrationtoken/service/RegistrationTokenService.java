package com.pawlak.subscription.token.registrationtoken.service;

import com.pawlak.subscription.exception.domain.RegistrationTokenExpiredException;
import com.pawlak.subscription.exception.domain.RegistrationTokenNotFoundException;
import com.pawlak.subscription.token.registrationtoken.model.RegistrationToken;
import com.pawlak.subscription.token.registrationtoken.repository.RegistrationTokenRepository;
import com.pawlak.subscription.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationTokenService {

    private final RegistrationTokenRepository registrationTokenRepository;

    public void createToken(User user) {

        String token = UUID.randomUUID().toString();

        RegistrationToken registrationUserToken = new RegistrationToken(user,token);

        registrationTokenRepository.save(registrationUserToken);
    }

    public void confirmRegistration(String token) {
        RegistrationToken registrationToken = registrationTokenRepository.findByToken(token)
                .orElseThrow(RegistrationTokenNotFoundException::new);

        if (registrationToken.getExpiredTime().isBefore(LocalDateTime.now())) {
            registrationTokenRepository.delete(registrationToken);
            throw new RegistrationTokenExpiredException("Token has expired");
        }

        registrationToken.getUser().setEnabled(true);
        registrationTokenRepository.delete(registrationToken);
    }
}
