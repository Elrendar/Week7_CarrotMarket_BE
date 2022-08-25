package com.sparta.velog.controller;

import com.sparta.velog.domain.CommentEntity;
import com.sparta.velog.dto.CommentRequestDto;
import com.sparta.velog.service.CommentService;
import com.sparta.velog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/comment")
    public ResponseEntity<List<CommentEntity>> readAllComment(@PathVariable Long postId){
        return ResponseEntity.ok(commentService.getAllComments(postId));
    }
    @PostMapping("/comment") //댓글 생성
    public ResponseEntity<CommentEntity> createComment(@RequestBody CommentRequestDto requestDto , @PathVariable Long postId){
        var userId = SecurityUtil.getCurrentUserIdByLong();
        return ResponseEntity.ok(commentService.commentCreate(userId, postId, requestDto));
    }
    @PutMapping("/comment/{commentId}") //댓글 수정
    public ResponseEntity<CommentEntity> commentUpdate(@RequestBody CommentRequestDto requestDto, @PathVariable Long postId, @PathVariable Long commentId){
        //로그인 여부 확인
        var userId = SecurityUtil.getCurrentUserIdByLong();
        return ResponseEntity.ok(commentService.commentUpdate(userId, postId, commentId, requestDto  ));
    }

    @DeleteMapping("/comment/{commentId}") //댓글 삭제
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId){
        //로그인 여부 확인
        var userId = SecurityUtil.getCurrentUserIdByLong();
        if (userId == 0) {
            throw new NullPointerException("로그인이 안되어있습니다.");
        }
        commentService.commentDelete(commentId,userId);
        return ("200 댓글이 삭제되었습니다.");
    }
}



//댓글작성
///api/posts/{postId}/comments
//수정
///api/posts/{postId}/comments/{commentId}
//삭제
///api/posts/{postId}/comments/{commentId}