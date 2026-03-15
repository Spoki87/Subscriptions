package com.pawlak.subscription.user.service;

import com.pawlak.subscription.currency.Currency;
import com.pawlak.subscription.exception.domain.InvalidPasswordException;
import com.pawlak.subscription.exception.domain.UserNotFoundException;
import com.pawlak.subscription.security.refresh.RefreshTokenService;
import com.pawlak.subscription.token.registrationtoken.service.RegistrationTokenService;
import com.pawlak.subscription.token.resetpasswordtoken.service.ResetPasswordTokenService;
import com.pawlak.subscription.user.dto.request.*;
import com.pawlak.subscription.user.dto.response.UserResponse;
import com.pawlak.subscription.user.model.User;
import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.repository.UserRepository;
import com.pawlak.subscription.exception.domain.EmailAlreadyTakenException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RegistrationTokenService registrationTokenService;
    private final ResetPasswordTokenService resetPasswordTokenService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public UserResponse register(CreateUserRequest request){

        boolean userExist = userRepository.findByEmail(request.getEmail()).isPresent();

        if(userExist){
            throw new EmailAlreadyTakenException();
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                bCryptPasswordEncoder.encode(request.getPassword()),
                Role.USER);

        userRepository.save(user);
        registrationTokenService.createToken(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCurrency()
        );
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if(!bCryptPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            throw new InvalidPasswordException();
        }
        user.changePassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
    }

    public void confirmRegistration(String token) {
        registrationTokenService.confirmRegistration(token);
    }

    public void resetPassword(ResetPasswordRequest request) {
        resetPasswordTokenService.createToken(request.getEmail());
    }

    @Transactional
    public void changeCurrency(ChangeCurrencyRequest request, User user) {
        user.changeCurrency(request.getCurrency());
        userRepository.save(user);
    }

    @Transactional
    public void setNewPassword(User user, NewPasswordRequest request) {
        resetPasswordTokenService.confirmToken(request.getToken());
        user.changePassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
    }
}
