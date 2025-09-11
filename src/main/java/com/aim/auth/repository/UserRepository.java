package com.aim.auth.repository;

import com.aim.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Id로 찾기
    Optional<User> findByUserId(Long userId);

    // 이메일
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Google Id로 찾기
    Optional<User> findByProviderId(String providerId);
}
