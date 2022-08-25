package com.sparta.velog.service;

import com.sparta.velog.domain.*;
import com.sparta.velog.dto.PostDetailResponseDto;
import com.sparta.velog.dto.PostListResponseDto;
import com.sparta.velog.dto.PostRequestDto;
import com.sparta.velog.exception.UnAuthorizedException;
import com.sparta.velog.exception.runtime.PostNotFoundException;
import com.sparta.velog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;
    private final PostTagRepository postTagRepository;
    private final LikeRepository likeRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    /* search */
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getPostPages(String searchKeyword, Pageable pageable) {
        // 검색어가 없으면 그냥 전체글을 반환
        if (searchKeyword == null) {
            return postRepository.findAll(pageable).map(PostListResponseDto::of);
        }
        // 검색어가 있으면
        return postRepository.findByTitleContaining(searchKeyword, pageable).map(PostListResponseDto::of);
    }

    // public List<PostResposeSearchDto> search(String keyword) {
    //     List<PostEntity> postEntities = postRepository.findByTitleContaining(keyword);
    //     List<PostResposeSearchDto> postListResponseDto = new ArrayList<>();
    //     if (postEntities.isEmpty()) return postListResponseDto;
    //     for (PostEntity postEntity : postEntities) {
    //         postListResponseDto.add(this.convertEntityToDto(postEntity));
    //     }
    //     return postListResponseDto;
    // }

    // private PostResposeSearchDto convertEntityToDto(PostEntity postEntities) {
    //     return PostResposeSearchDto.builder()
    //             .userId(postEntities.getUserId())
    //             .postId(postEntities.getId())
    //             .title(postEntities.getTitle())
    //             .content(postEntities.getContent())
    //             .profileImageUrl(postEntities.getUser().getProfileImageUrl())
    //             .likeCount(postEntities.getLikeCount())
    //             .build();
    // }

    @Transactional
    public long createPost(long userId, PostRequestDto postRequestDto) {
        // 작성자 매핑을 위한 UserEntity 객체 생성
        var user = UserEntity.builder()
                .id(userId)
                .build();
        // 작성글 생성
        var post = PostEntity.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .user(user)
                .build();

        // 작성글 저장
        post = postRepository.save(post);

        // 이미지 업로드 부분
        // 업로드할 이미지 파일이 있는지 확인
        if (postRequestDto.getImageFiles() != null) {
            for (var imageFile : postRequestDto.getImageFiles()) {
                // 이미지를 s3에 업로드 하고
                var uploadedImageUrl = s3Service.uploadImage(imageFile);
                // db에 저장
                postImageRepository.save(
                        PostImageEntity.builder()
                                .url(uploadedImageUrl)
                                .post(post)
                                .user(user)
                                .build());
            }
        }

        // 태그 추가 부분
        // 태그가 null이 아니라면
        if (postRequestDto.getTags() != null) {
            // HashtagEntity 생성해서 저장하고, 작성글과 해쉬태그를 토대로 PostTagEntity 생성해서 저장
            createPostTag(post, createHashTag(postRequestDto.getTags()));
        }

        return post.getId();
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
    public long updatePost(long currentUserId, long postId,
                           PostRequestDto postRequestDto) {
        // 수정할 글 불러오기
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(
                        "postId: " + postId + "인 글이 존재하지 않습니다.")
                );
        // 로그인한 유저가 작성자와 동일한 유저인지 확인
        if (currentUserId != post.getUserId()) {
            throw new UnAuthorizedException("자신이 작성한 글만 수정 가능합니다!");
        }

        // 제목과 내용 수정
        post.setTitle(postRequestDto.getTitle());
        post.setContent(postRequestDto.getContent());

        // // 이미지 수정
        // String imageUrl = null;

        // // 새로운 이미지가 있으면 새로운 파일을 넣고 기존 파일 삭제
        // if (Objects.nonNull(postRequestDto.getImageFiles())) {
        //     // 새로운 이미지 파일 s3 버킷 저장
        //     imageUrl = s3Service.uploadImage(postRequestDto.getImageFiles().get(0));
        // } else {
        //     // 이미지 파일이 Null 값이면 기존 이미지 그대로 사용함.
        //     // imageUrl = post.getImageUrl();
        // }

        // 업로드할 이미지 파일이 있는지 확인
        if (postRequestDto.getImageFiles() != null) {
            // 기존 이미지 모두 삭제
            var deletedImages = postImageRepository.deleteAllByPostId(postId);
            // 기존 이미지 파일 삭제 (내부에 Url -> filename 으로 분리 로직 존재)
            for (var postImage : deletedImages) {
                s3Service.deleteObjectByImageUrl(postImage.getUrl());
            }
            // 새로운 이미지 추가
            for (var imageFile : postRequestDto.getImageFiles()) {
                // 이미지를 s3에 업로드 하고
                var uploadedImageUrl = s3Service.uploadImage(imageFile);
                // db에 저장
                postImageRepository.save(
                        PostImageEntity.builder()
                                .url(uploadedImageUrl)
                                .post(post)
                                .user(post.getUser())
                                .build());
            }
        }

        // 기존 태그목록 비어있지 않으면
        if (!post.getPostTags().isEmpty()) {
            // 모든 태그 삭제
            postTagRepository.deleteAllByPostId(postId);
        }

        // 새로운 태그를 생성해서 추가
        createPostTag(post, createHashTag(postRequestDto.getTags()));
        return post.getId();
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

        // s3 버킷에서 기존 이미지 삭제
        for (var postImage : post.getImages()) {
            s3Service.deleteObjectByImageUrl(postImage.getUrl());
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

    private List<TagEntity> createHashTag(List<String> tags) {
        if (tags == null) {
            return null;
        }

        // 태그 List<string>를 HashtagEntity로 변환해서 db에 저장
        var newTags = new ArrayList<TagEntity>();
        for (var tag : tags) {
            var newTag = TagEntity.builder()
                    .tagString(tag)
                    .build();
            newTags.add(newTag);
        }
        return hashtagRepository.saveAll(newTags);
    }

    private void createPostTag(PostEntity post,
                               List<TagEntity> tags) {
        if (tags == null) {
            return;
        }

        if (post == null) {
            throw new PostNotFoundException("Unknown error: post가 null입니다.");
        }

        // post와 hashtag 간의 다대다 매핑을 연결해줄 중간 테이블 PostTag 생성해서 저장
        var postTags = new ArrayList<PostTagEntity>();
        for (var tag : tags) {
            var postTag = PostTagEntity.builder()
                    .post(post)
                    .tag(tag)
                    .build();
            postTags.add(postTag);
        }
        var savedPostTags = postTagRepository.saveAll(postTags);

        if (post.getPostTags() != null) {
            post.getPostTags().clear();
            post.getPostTags().addAll(savedPostTags);
        }
    }

    public Page<PostListResponseDto> getMyPosts(long userId, Pageable pageable) {
        // 검색어가 있으면
        return postRepository.findByUserId(userId, pageable).map(PostListResponseDto::of);
    }

    public Page<PostListResponseDto> getLikedPosts(long userId, Pageable pageable) {
        // 검색어가 있으면
        var user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UsernameNotFoundException("userId: " + userId + "는 존재하지 않는 아이디입니다.")
                );
        if (user.getLikePosts() == null) {
            return null;
        }

        ArrayList<PostEntity> likedPosts = new ArrayList<>();

        for (var likePost : user.getLikePosts()) {
            likedPosts.add(likePost.getPost());
        }

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), likedPosts.size());

        return new PageImpl<>(likedPosts.subList(start, end), pageable, likedPosts.size()).map(PostListResponseDto::of);
    }
}
