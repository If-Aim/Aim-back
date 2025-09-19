package com.aim.global.config;

import com.aim.auth.service.jwt.JwtAuthenticationFilter;
import com.aim.auth.service.jwt.JwtResolver;
import com.aim.auth.service.jwt.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtResolver jwtResolver, UserDetailsService userDetailsService) throws Exception {

        http
                // 요청 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()           // 에러 페이지 허용
                        .requestMatchers("/login/**").permitAll()        // OAuth2 콜백 허용
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/auth/student/**").hasRole("STUDENT")
                        .requestMatchers("/auth/company/**").hasRole("COMPANY")
                        .anyRequest().authenticated()
                )
                // OAuth2 설정 제거 - 커스텀 코드 사용
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable) //CSRF 비활성화
                .sessionManagement(session -> // 세션을 쓰지 않는다면 stateless
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // HSTS 헤더 비활성화
                .headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts.disable()))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtResolver, userDetailsService, customAuthenticationEntryPoint),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // RestTemplate Bean 추가
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 타임아웃 설정 (SimpleClientHttpRequestFactory 사용)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 연결 타임아웃: 5초
        factory.setReadTimeout(10000);    // 읽기 타임아웃: 10초

        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}
