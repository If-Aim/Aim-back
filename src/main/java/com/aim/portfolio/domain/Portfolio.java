package com.aim.portfolio.domain;
import com.aim.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String information;

    private String portfolioImageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 수정
    public void changeTitle(String title){this.title = title;}
    public void changeInformation(String information){this.information = information;}
    public void changeImageUrl(String portfolioImageUrl){this.portfolioImageUrl = portfolioImageUrl;}
}
