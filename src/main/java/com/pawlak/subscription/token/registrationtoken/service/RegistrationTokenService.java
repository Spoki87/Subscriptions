package com.pawlak.subscription.token.registrationtoken.service;

import com.pawlak.subscription.token.registrationtoken.model.RegistrationToken;
import com.pawlak.subscription.token.registrationtoken.repository.RegistrationTokenRepository;
import com.pawlak.subscription.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
