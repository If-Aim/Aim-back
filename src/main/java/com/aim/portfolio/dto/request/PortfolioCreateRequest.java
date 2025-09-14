package com.aim.portfolio.dto.request;

import com.aim.portfolio.domain.Portfolio;
import com.aim.auth.domain.User;
import jakarta.validation.constraints.NotNull;

public record PortfolioCreateRequest(@NotNull String title,
                                     @NotNull String information,
                                     String portfolioImageUrl){
    public Portfolio toEntity(User user){
        return Portfolio.builder()
                .title(title())
                .information(information())
                .portfolioImageUrl(portfolioImageUrl())
                .user(user)
                .build();
    }

}