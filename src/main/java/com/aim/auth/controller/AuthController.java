package com.aim.auth.controller;

import com.aim.auth.dto.request.EmailLoginRequest;
import com.aim.auth.dto.request.GoogleOAuth2Request;
import com.aim.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/auth/test")
    public ResponseEntity<Void> test(){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public ResponseEntity<Void> loginOrRegister(@RequestBody EmailLoginRequest request,
                                                HttpServletResponse response) {
        authService.loginOrRegisterUser(request, response);
        return ResponseEntity.ok().build();
    }

    // 구글 OAuth2 code로 JWT 토큰 발급 - 포스트맨
    @PostMapping("/auth/oauth2/google")
    @ResponseBody
    public ResponseEntity<String> googleOAuth2Login(@RequestBody GoogleOAuth2Request request,
                                                   HttpServletResponse response) {
        String jwtToken = authService.googleOAuth2Login(request.getCode(), response);
        return ResponseEntity.ok(jwtToken);
    }

    // 구글 OAuth2 리다이렉트 핸들러 - 인가코드 표시
    @GetMapping("/login/oauth2/code/google")
    @ResponseBody
    public String handleGoogleRedirect(@RequestParam String code, 
                                     @RequestParam(required = false) String state) {
        return "인가코드를 받았습니다: " + code + 
               "<br><br>이제 포스트맨으로 다음 요청을 보내세요:" +
               "<br>POST http://localhost:8080/auth/oauth2/google" +
               "<br>Body: {\"code\": \"" + code + "\"}";
    }
}
