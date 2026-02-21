package com.pawlak.subscription.appuser.service;

import com.pawlak.subscription.appuser.dto.request.CreateUserRequest;
import com.pawlak.subscription.appuser.dto.response.UserResponse;
import com.pawlak.subscription.appuser.model.Appuser;
import com.pawlak.subscription.appuser.model.Role;
import com.pawlak.subscription.appuser.repository.UserRepository;
import com.pawlak.subscription.exception.domain.EmailAlreadyTakenException;
import com.pawlak.subscription.exception.domain.UserNotFoundException;
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

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserResponse register(CreateUserRequest request){

        boolean userExist = userRepository.findByEmail(request.getEmail()).isPresent();

        if(userExist){
            throw new EmailAlreadyTakenException();
        }

        Appuser user = new Appuser(
                request.getUsername(),
                request.getEmail(),
                bCryptPasswordEncoder.encode(request.getPassword()),
                Role.USER);

        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
