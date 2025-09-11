package com.aim.auth.service.jwt;

import com.aim.auth.domain.User;
import com.aim.auth.repository.UserRepository;
import com.aim.global.exception.AimException;
import com.aim.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService{
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AimException(ExceptionCode.USER_NOT_FOUND));
        return new CustomUserDetails(user);
    }
}
