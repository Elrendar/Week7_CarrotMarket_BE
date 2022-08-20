package com.sparta.velog.service;

import com.sparta.velog.domain.PostEntity;
import com.sparta.velog.repository.PostRepository;
import com.sparta.velog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    /* search */
    @Transactional
    public List<PostEntity> search(String keyword) {
        List<PostEntity> postsList = postRepository.findByTitleContaining(keyword);
        return postsList;
    }
}
