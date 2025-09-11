package com.aim.auth.service.jwt;

/*
- `login(email, rawPassword)` → `UserRepository` 조회 → `PasswordEncoder.matches` → Access/Refresh 발급.
- `reissue(refreshToken)` → 검증 후 새 Access 발급(필요시 refresh 회전).
 */

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

}

