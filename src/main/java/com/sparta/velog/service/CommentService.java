package com.sparta.velog.service;

import com.sparta.velog.domain.CommentEntity;
import com.sparta.velog.domain.PostEntity;
import com.sparta.velog.dto.CommentRequestDto;
import com.sparta.velog.repository.CommentRepository;
import com.sparta.velog.repository.PostRepository;
import com.sparta.velog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private  final UserService userService;

    //댓글 조회
    public List<CommentEntity> getAllComments(Long postId) {
        return commentRepository.findAllByPostEntity_Id(postId);
    }

    public CommentEntity commentCreate (CommentRequestDto requestDto, Long postId){
        PostEntity post = postRepository.findById(postId).orElseThrow(()->
                new NullPointerException("미정"));
        String contents = requestDto.getContents();
        if (contents.length() < 1){
            throw new NullPointerException("작성을 부탁드립니다.");
        }
        CommentEntity comment = new CommentEntity(post, requestDto.getContents());
        var userId = SecurityUtil.getCurrentUserIdByLong();
        comment.setUsername(userService.getMyInfo().getUsername());

        commentRepository.save(comment);
        return comment;

    }


}
