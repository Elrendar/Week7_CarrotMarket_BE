package com.sparta.velog.repository;

import com.sparta.velog.domain.PostTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTagEntity, Long> {
    void deleteAllByPostId(long postId);
}
