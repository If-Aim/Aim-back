package com.aim.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleUserInfo {
    private String id;
    private String email;
    private String name;
    private String picture;
    
    @JsonProperty("verified_email")
    private boolean verifiedEmail;
}

