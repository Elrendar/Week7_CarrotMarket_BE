package com.sparta.velog.repository;

import com.sparta.velog.domain.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // Containing을 붙여주면 Like 검색이 가능해진다.
    List<PostEntity> findByTitleContaining(String keyword);

    // Page<PostEntity> findByTagsContaining(String postTag, Pageable pageable);
}
