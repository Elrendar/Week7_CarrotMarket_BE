package com.sparta.velog.repository;

import com.sparta.velog.domain.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    Optional<LikeEntity> findByUserIdAndPostId(long userId, long postId);

    List<LikeEntity> findAllByPostId(long postId);
}
