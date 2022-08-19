package com.sparta.carrot.repository;

import com.sparta.carrot.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // username 중복체크
    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);
}
