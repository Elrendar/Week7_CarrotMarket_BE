package com.sparta.velog.repository;

import com.sparta.velog.domain.PostImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImageEntity, Long> {
    List<PostImageEntity> deleteAllByPostId(long postId);
}
