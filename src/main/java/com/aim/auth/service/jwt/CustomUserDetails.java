package com.aim.auth.service.jwt;


import com.aim.auth.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
/* Security 아는 사용자 표현체
getUsername()은 “로그인 식별자(이메일/ID)”와 일치시켜라.
getPassword()는 이메일+비번 로그인 시 반드시 해시값을 반환. (JWT만 쓸 땐 의미 적음)
*/

public class CustomUserDetails implements UserDetails, OAuth2User {
    //Principal 자바 표준 보안 인터페이스, getName() only 요구

    private final User user;
    private Map<String, Object> attributes;
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 권한 부여
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    // 해시(BCrypt) 값으로 Password 반환 / Email 반환
    @Override public String getPassword() {return user.getPassword();}
    @Override public String getUsername() {return user.getEmail();}

    // 모두 참이라면 만료/잠금/자격만료/비활성화 없음
    @Override public boolean isAccountNonExpired() {
        return true;
    }
    @Override public boolean isAccountNonLocked() {
        return true;
    }
    @Override public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override public boolean isEnabled() {
        return true;
    }

    // OAuth2User
    @Override public Map<String, Object> getAttributes() {return attributes;}
    @Override public String getName() { return String.valueOf(user.getUserId());}
}