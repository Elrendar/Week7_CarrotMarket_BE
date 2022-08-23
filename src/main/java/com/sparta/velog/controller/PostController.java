package com.sparta.velog.controller;

import com.sparta.velog.domain.PostEntity;
import com.sparta.velog.dto.PostDetailResponseDto;
import com.sparta.velog.dto.PostListResponseDto;
import com.sparta.velog.dto.PostRequestDto;
import com.sparta.velog.service.PostService;
import com.sparta.velog.service.S3Service;
import com.sparta.velog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RequestMapping("/api/posts")
@RestController
public class PostController {
    private final PostService postService;


    // 작성글 검색
    // 작성글 검색
    @GetMapping
    public ResponseEntity<List<PostEntity>> search(@RequestParam(name = "query") String keyword) {
        return ResponseEntity.ok(postService.search(keyword));
    }

    // 글 목록 불러오기
    @GetMapping
    public ResponseEntity<Page<PostListResponseDto>> getPostPages(@RequestParam(required = false) String searchKeyword,
                                                                  @PageableDefault(size = 5,
                                                                          sort = "createdAt",
                                                                          direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getPostPages(searchKeyword, pageable));
    }

    // 글 작성하기
    @PostMapping
    public ResponseEntity<Long> createPost(PostRequestDto postRequestDto) {
        var userId = SecurityUtil.getCurrentUserIdByLong();

        return ResponseEntity.ok(
                postService.createPost(userId, postRequestDto));
    }
    //return 값 Long으로 할지, postEntity로 할지.

    // 글 읽기
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> readPost(@PathVariable long postId) {
        return ResponseEntity.ok(
                postService.readPost(postId));
    }

    // 글 수정하기
    @PutMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> updatePost(@PathVariable long postId,
                                                            @RequestBody PostRequestDto postRequestDto) {
        var userId = SecurityUtil.getCurrentUserIdByLong();
        return ResponseEntity.ok(
                postService.updatePost(userId, postId, postRequestDto));
    }

    // 글 삭제하기
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable long postId) {
        var userId = SecurityUtil.getCurrentUserIdByLong();
        postService.deletePost(userId, postId);
        return ResponseEntity.ok("글이 성공적으로 삭제되었습니다.");
    }

    // 좋아요 등록하기
    @PostMapping("/{postId}/likes")
    public ResponseEntity<Integer> likePost(@PathVariable long postId) {
        var userId = SecurityUtil.getCurrentUserIdByLong();
        return ResponseEntity.ok(postService.likePost(userId, postId));
    }
}
