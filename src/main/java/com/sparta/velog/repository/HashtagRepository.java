package com.sparta.velog.repository;

import com.sparta.velog.domain.HashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<HashtagEntity, Long> {
}
