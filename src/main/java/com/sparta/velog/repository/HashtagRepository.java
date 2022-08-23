package com.sparta.velog.repository;

import com.sparta.velog.domain.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<TagEntity, Long> {
}
