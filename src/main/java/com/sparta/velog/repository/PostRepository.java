package com.sparta.velog.repository;

import com.sparta.velog.domain.PostEntity;
import com.sparta.velog.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByTitleContaining(String keyword);
    //위와 같이 Containing을 붙여주면 Like 검색이 가능해진다.
}
