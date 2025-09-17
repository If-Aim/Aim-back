package com.aim.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleOAuth2Request {
    private String code;
}

