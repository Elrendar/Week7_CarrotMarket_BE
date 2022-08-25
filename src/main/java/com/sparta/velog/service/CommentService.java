package com.sparta.velog.service;

import com.sparta.velog.domain.CommentEntity;
import com.sparta.velog.domain.PostEntity;
import com.sparta.velog.domain.UserEntity;
import com.sparta.velog.dto.CommentRequestDto;
import com.sparta.velog.exception.UnAuthorizedException;
import com.sparta.velog.repository.CommentRepository;
import com.sparta.velog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
//    private final CommentRequestDto requestDto;

    // 댓글 조회
    public List<CommentEntity> getAllComments(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    @Transactional
    // 댓글 생성
    public CommentEntity commentCreate(Long userId, Long postId,
                                       CommentRequestDto requestDto) {
        String contents = requestDto.getComment();
        if (contents.length() < 1) {
            throw new NullPointerException("작성을 부탁드립니다.");
        }
        var user = UserEntity.builder()
                .id(userId)
                .build();
        var post = PostEntity.builder()
                .id(userId)
                .build();

        CommentEntity newComment = CommentEntity.builder()
                .user(user)
                .PostEntity(post)
                .comment(contents)
                .build();

        post.addCommentCount();

        return commentRepository.save(newComment);
    }

    @Transactional
    public CommentEntity commentUpdate(long userId, long postId, long commentId,
                                       CommentRequestDto requestDto) {
        // 수정할 댓글 찾기 (사실 코멘트아이디가 PK 라서 포스팅아이디 필요없음)

        CommentEntity comment = commentRepository.findByCommentId(commentId);
        if (comment == null) {
            throw new NullPointerException("코멘트가 없습니다.");
        }
        long test = comment.getUserId();
        var post = comment.getPostEntity();
        // 댓글 작성자와 로그인된 유저 비교
        if (userId != comment.getUserId()) {
            throw new UnAuthorizedException("자신이 작성한 글만 수정 가능합니다!");
        }
        // 댓글 정보 업데이트
        String comments = requestDto.getComment();
        if (comments.length() < 1) {
            throw new NullPointerException("댓글이 없습니다.!");
        }
        comment.setComment(requestDto.getComment());

        return comment;
    }

    @Transactional
    public Long commentDelete(Long commentId, Long userId) {
        // 삭제할 댓글 찾기
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("삭제할 댓글이 없습니다."));
        // 댓글 작섣자와 로그인된 유저 비교
        if (!comment.getUserId().equals(userId)) {
            throw new EntityNotFoundException("로그인 유저가 아닙니다");
        }
        // 댓글 갯수 감소
        comment.getPostEntity().minusCommentCount();
        // 댓글 데이터 삭제
        commentRepository.deleteById(commentId);
        return commentId;
    }


}
