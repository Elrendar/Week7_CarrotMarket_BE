package com.sparta.velog.repository;

import com.sparta.velog.domain.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // Containing을 붙여주면 Like 검색이 가능해진다.
    Page<PostEntity> findByTitleContaining(String keyword, Pageable pageable);

    Page<PostEntity> findByUserId(long userId, Pageable pageable);
}
