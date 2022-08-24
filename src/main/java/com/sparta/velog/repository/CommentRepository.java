package com.sparta.velog.repository;

import com.sparta.velog.domain.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByPostEntity_Id(Long postingId);
    CommentEntity findByPostEntity_IdAndCommentsId(Long postingId, Long commentId);
}