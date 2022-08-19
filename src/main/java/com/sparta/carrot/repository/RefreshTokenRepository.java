package com.sparta.carrot.repository;

import com.sparta.carrot.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
