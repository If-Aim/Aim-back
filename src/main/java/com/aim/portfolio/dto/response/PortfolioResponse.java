package com.aim.portfolio.dto.response;

import com.aim.portfolio.domain.Portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PortfolioResponse {

    private Long portfolioId;
    private String title;
    private String information;
    private String portfolioImageUrl;
    private Long userId;
    private String userEmail;

    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getPortfolioId())
                .title(portfolio.getTitle())
                .information(portfolio.getInformation())
                .portfolioImageUrl(portfolio.getPortfolioImageUrl())
                .userId(portfolio.getUser().getUserId())
                .userEmail(portfolio.getUser().getEmail())
                .build();
    }
}