package com.aim.portfolio.repository;
import com.aim.portfolio.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
