package com.sparta.velog.controller;

import com.sparta.velog.domain.CommentEntity;
import com.sparta.velog.dto.CommentRequestDto;
import com.sparta.velog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/comment")
    public List<CommentEntity> readAllComment(@PathVariable Long postId){
        return commentService.getAllComments(postId);
    }
    @PostMapping("/comment") //댓글 생성
    public CommentEntity createComment(@RequestBody CommentRequestDto requestDto, @PathVariable Long postId){
        return commentService.commentCreate(requestDto, postId);

    }
}



//댓글작성
///api/posts/{postId}/comments
//수정
///api/posts/{postId}/comments/{commentId}
//삭제
///api/posts/{postId}/comments/{commentId}