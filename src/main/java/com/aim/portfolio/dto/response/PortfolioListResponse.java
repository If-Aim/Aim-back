package com.aim.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PortfolioListResponse {
    private Integer totalCount;
    private List<PortfolioResponse> portfolioList;

    public static PortfolioListResponse from(List<PortfolioResponse> portfolioList){
        return PortfolioListResponse.builder()
                .totalCount(portfolioList.size())
                .portfolioList(portfolioList)
                .build();
    }
}