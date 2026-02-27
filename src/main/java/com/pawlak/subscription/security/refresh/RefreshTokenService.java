package com.pawlak.subscription.security.refresh;

import com.pawlak.subscription.exception.domain.InvalidRefreshTokenException;
import com.pawlak.subscription.exception.domain.RefreshTokenExpiredException;
import com.pawlak.subscription.security.jwt.JwtProperties;
import com.pawlak.subscription.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final TokenHashService tokenHashService;


    public RefreshToken createInitialToken(User user) {
        String rawToken = generateRawToken();

        RefreshToken refreshToken = new RefreshToken(
                tokenHashService.hash(rawToken),
                now().plus(jwtProperties.getRefreshTokenDuration()),
                now().plus(jwtProperties.getSessionDuration()),
                user
        );
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken rotateToken(String rawToken){
        RefreshToken currentToken = validate(rawToken);

        currentToken.revoke();
        refreshTokenRepository.save(currentToken);

        String newRawToken = generateRawToken();
        RefreshToken newRefreshToken = new RefreshToken(
                tokenHashService.hash(newRawToken),
                now().plus(jwtProperties.getRefreshTokenDuration()),
                currentToken.getSessionExpiryDate(),
                currentToken.getUser()
        );
        refreshTokenRepository.save(newRefreshToken);
        return newRefreshToken;
    }

    public RefreshToken validate(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHashService.hash(token))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            throw new RefreshTokenExpiredException();
        }

        if(refreshToken.getSessionExpiryDate().isBefore(now())){
            throw new RefreshTokenExpiredException();
        }

        return refreshToken;
    }

    private String generateRawToken() {
        return UUID.randomUUID().toString();
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
