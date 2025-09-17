package com.aim.portfolio.service;


import com.aim.portfolio.domain.Portfolio;
import com.aim.portfolio.dto.request.PortfolioCreateRequest;
import com.aim.portfolio.dto.request.PortfolioUpdateRequest;
import com.aim.portfolio.dto.response.PortfolioListResponse;
import com.aim.portfolio.dto.response.PortfolioResponse;
import com.aim.portfolio.repository.PortfolioRepository;
import com.aim.auth.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    // 포트폴리오 생성
    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreateRequest request, User user) {
        Portfolio portfolio = portfolioRepository.save(request.toEntity(user));
        return PortfolioResponse.from(portfolio);
    }

    // 포트폴리오 수정
    @Transactional
    public PortfolioResponse updatePortfolio(Long portfolioId, PortfolioUpdateRequest request, User user){
        Portfolio portfolio = getPortfolio(portfolioId);
        authorizeUser(user, portfolio);

        if(request.title()!=null){portfolio.changeTitle(request.title());}
        if(request.information()!=null){portfolio.changeInformation(request.information());}
        if(request.portfolioImageUrl()!=null){portfolio.changeImageUrl(request.portfolioImageUrl());}

        return PortfolioResponse.from(portfolio);
    }


    // 포트폴리오 삭제
    @Transactional
    public void deletePortfolio(Long portfolioId, User user){
        Portfolio portfolio = getPortfolio(portfolioId);
        authorizeUser(user, portfolio);
        portfolioRepository.delete(portfolio);
    }

    // 포트폴리오 하나 조회
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioOne(Long portfolioId){
        Portfolio portfolio = getPortfolio(portfolioId);
        return PortfolioResponse.from(portfolio);
    }

    // 포트폴리오 여러개 조회
    @Transactional(readOnly = true)
    public PortfolioListResponse getPortfolios(){
        List<Portfolio> portfolioList = portfolioRepository.findAll();

        List<PortfolioResponse> responseList = portfolioList.stream()
                .map(PortfolioResponse::from).toList();

        return PortfolioListResponse.from(responseList);
    }


/*
----------------------------------------------------------------------
 */

    // portfolioId로 Portfolio 조회
    @Transactional(readOnly = true)
    private Portfolio getPortfolio(Long portfolioId){
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("해당 포트폴리오가 존재하지 않습니다."));
    }

    // 수정, 삭제 권한 확인
    private void authorizeUser(User user, Portfolio portfolio){
        if(!user.getUserId().equals(portfolio.getUser().getUserId())){
            throw new RuntimeException("권한이 없습니다.");
        }
    }


}
