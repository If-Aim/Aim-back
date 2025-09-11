package com.aim.auth.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Column
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

   // provider : Google
   private String provider;

    // providerId : 구글 로그인 한 유저의 고유 ID
    @Column(length = 100)
    private String providerId;


    public enum Role {
        ROLE_STUDENT,
        ROLE_COMPANY,
        ROLE_ADMIN
    }

    @Builder
    public User(String email, String name, String imageUrl, Role role, String provider, String providerId){
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }
}

