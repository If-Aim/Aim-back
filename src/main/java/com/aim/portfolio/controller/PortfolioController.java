package com.aim.portfolio.controller;

import com.aim.auth.domain.User;
import com.aim.portfolio.dto.request.PortfolioCreateRequest;
import com.aim.portfolio.dto.request.PortfolioUpdateRequest;
import com.aim.portfolio.dto.response.PortfolioListResponse;
import com.aim.portfolio.dto.response.PortfolioResponse;
import com.aim.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    // 포트폴리오 생성
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@AuthenticationPrincipal User user,
                                                             @Valid @RequestBody PortfolioCreateRequest request) {
        PortfolioResponse response = portfolioService.createPortfolio(request, user);
        Long portfolioId = response.getPortfolioId();
        return ResponseEntity.created(URI.create("/portfolios/"+portfolioId)).body(response);
    }

    // 포트폴리오 수정
    @PatchMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(@PathVariable("portfolioId") Long portfolioId,
                                                             @AuthenticationPrincipal User user,
                                                             @Valid @RequestBody PortfolioUpdateRequest request){
        PortfolioResponse response = portfolioService.updatePortfolio(portfolioId, request, user);
        return ResponseEntity.ok(response);
    }


    // 포트폴리오 삭제
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable("portfolioId") Long portfolioId,
                                                @AuthenticationPrincipal User user){
        portfolioService.deletePortfolio(portfolioId, user);
        return ResponseEntity.noContent().build();
    }

    // 포트폴리오 조회
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable("portfolioId") Long portfolioId){
        PortfolioResponse response = portfolioService.getPortfolioOne(portfolioId);
        return ResponseEntity.ok(response);
    }

    // 포트폴리오 전체 조회
    @GetMapping
    public ResponseEntity<PortfolioListResponse> getPortfolios(){
        PortfolioListResponse response = portfolioService.getPortfolios();
        return ResponseEntity.ok(response);
    }



}
