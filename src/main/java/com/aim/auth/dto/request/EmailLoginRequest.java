package com.aim.auth.dto.request;

import com.aim.auth.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class EmailLoginRequest {
    @Email
    private String email;
    @NotNull
    private String password;
    @NotNull
    private User.Role role;
}

