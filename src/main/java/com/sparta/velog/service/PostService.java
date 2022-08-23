package com.sparta.velog.service;

import com.sparta.velog.domain.*;
import com.sparta.velog.dto.PostDetailResponseDto;
import com.sparta.velog.dto.PostListResponseDto;
import com.sparta.velog.dto.PostRequestDto;
import com.sparta.velog.exception.UnAuthorizedException;
import com.sparta.velog.exception.runtime.PostNotFoundException;
import com.sparta.velog.repository.HashtagRepository;
import com.sparta.velog.repository.LikeRepository;
import com.sparta.velog.repository.PostRepository;
import com.sparta.velog.repository.PostTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;
    private final PostTagRepository postTagRepository;
    private final LikeRepository likeRepository;

    private final S3Service s3Service;

    /* search */
    @Transactional(readOnly = true)
    public List<PostEntity> search(String keyword) {
        return postRepository.findByTitleContaining(keyword);
    }

    @Transactional
    public long createPost(long userId,
                           PostRequestDto postRequestDto) {
        String imageUrl = null;

        if (Objects.nonNull(postRequestDto.getImageFile())) {
            imageUrl = s3Service.uploadImage(postRequestDto.getImageFile());
        }

        // 작성자 매핑을 위한 UserEntity 객체 생성
        var user = UserEntity.builder()
                .id(userId)
                .build();
        // 작성글
        var newPost = PostEntity.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .imageUrl(imageUrl)
                .user(user)
                .build();

        // 작성글 저장
        newPost = postRepository.save(newPost);

        // 태그가 null이 아니라면
        if (postRequestDto.getTags() != null) {
            // HashtagEntity 생성해서 저장하고, 작성글과 해쉬태그를 토대로 PostTagEntity 생성해서 저장
            createPostTag(newPost, createHashTag(postRequestDto.getTags()));
        }

        return newPost.getId();
    }

    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getPostPages(String searchKeyword, Pageable pageable) {
        // 검색어가 없으면 그냥 전체글을 반환
        if (searchKeyword == null) {
            return postRepository.findAll(pageable).map(PostListResponseDto::of);
        }
        // 검색어가 있으면 -> 작업예정
        return null;
        // return postRepository.findByTagContaining(searchKeyword, pageable).map(PostListResponseDto::of);
    }

    @Transactional(readOnly = true)
    public PostDetailResponseDto readPost(long postId) {
        return postRepository.findById(postId)
                .map(PostDetailResponseDto::of)
                .orElseThrow(() -> new PostNotFoundException(
                        "postId: " + postId +
                                "인 글이 존재하지 않습니다."));
    }


    @Transactional
    public PostDetailResponseDto updatePost(long userId, long postId,
                                            PostRequestDto postRequestDto) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(
                        "postId: " + postId + "인 글이 존재하지 않습니다.")
                );
        // 로그인한 유저가 작성자와 동일한 유저인지 확인
        if (userId != post.getUserId()) {
            throw new UnAuthorizedException("자신이 작성한 글만 수정 가능합니다!");
        }

        var postTags = post.getPostTags();
        var newTags = postRequestDto.getTags();

        // 기존 태그목록이 비어있고
        if (postTags.isEmpty()) {
            // 새로운 태그목록이 비어있지 않다면
            if (newTags != null) {
                // 새 태그를 생성해서 추가
                createPostTag(post, createHashTag(newTags));
            }
            return PostDetailResponseDto.of(postRepository.save(post).update(postRequestDto));
        }

        // 모든 태그 삭제
        postTagRepository.deleteAllByPostId(postId);
        postTags.clear();

        // 새로운 태그를 생성해서 추가
        createPostTag(post, createHashTag(newTags));
        return PostDetailResponseDto.of(postRepository.save(post).update(postRequestDto));
    }

    @Transactional
    public void deletePost(long userId, long postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(
                        "postId: " + postId +
                                "인 글이 존재하지 않습니다."));

        // 로그인한 유저가 작성자와 동일한 유저인지 확인
        if (userId != post.getUserId()) {
            throw new UnAuthorizedException("자신이 작성한 글만 삭제 가능합니다!");
        }

        postRepository.delete(post);
    }

    @Transactional
    public int likePost(long userId, long postId) {
        // 기존에 좋아요를 누른 적 있는지 확인
        var like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElse(null);

        // 좋아요 처리할 게시글 검색
        var post = postRepository.findById(postId)
                .orElseThrow(
                        () -> new PostNotFoundException(
                                "postId: " + postId + "인 글이 존재하지 않습니다."));

        // 이전에 누른 적 있다면
        if (like != null) {
            // 삭제하고
            likeRepository.delete(like);
            // 좋아요 개수 감소
            return post.minusLikeCount();
        }

        // null 이면 누른 적이 없음
        // 좋아요 누른 유저 매핑을 위한 UserEntity 객체 생성
        var user = UserEntity.builder()
                .id(userId)
                .build();

        // LikeEntity 생성해서 db에 저장
        like = LikeEntity.builder()
                .post(post)
                .user(user)
                .build();

        likeRepository.save(like);

        return post.addLikeCount();
    }

    protected List<HashtagEntity> createHashTag(List<String> tags) {
        if (tags == null) {
            return null;
        }

        // 태그 List<string>를 HashtagEntity로 변환해서 db에 저장
        var hashtags = new ArrayList<HashtagEntity>();
        for (var tag : tags) {
            var newHashtag = HashtagEntity.builder()
                    .tag(tag)
                    .build();
            hashtags.add(newHashtag);
        }
        return hashtagRepository.saveAll(hashtags);
    }

    protected void createPostTag(PostEntity post,
                                 List<HashtagEntity> hashtags) {
        if (hashtags == null) {
            return;
        }

        if (post == null) {
            throw new PostNotFoundException("Unknown error: post가 null입니다.");
        }

        // post와 hashtag 간의 다대다 매핑을 연결해줄 중간 테이블 PostTag 생성해서 저장
        var postTags = new ArrayList<PostTagEntity>();
        for (var hashtag : hashtags) {
            var postTag = PostTagEntity.builder()
                    .post(post)
                    .hashtag(hashtag)
                    .build();
            postTags.add(postTag);
        }
        postTagRepository.saveAll(postTags);
        // post.setPostTags(postTags);

        // var savedPostTags = postTagRepository.saveAll(postTags);
        // post.setPostTags(savedPostTags);
    }
}
