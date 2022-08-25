package com.sparta.velog.dto;

import com.sparta.velog.domain.PostEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostDetailResponseDto {
    @NotBlank
    Long postId;
    @NotBlank
    String title;
    @NotBlank
    String content;
    // 이미지 url 조회
    List<String> imageUrls;
    @NotBlank
    String username;
    // 작성자 프로필 이미지 url
    @NotBlank
    String profileImageUrl;
    // 게시글 해쉬태그
    List<String> hashtags;
    // 게시글 좋아요 수
    int likeCount;
    // 댓글 수
    int commentCount;
    @NotBlank
    String createdAt;
    @NotBlank
    String modifiedAt;

    public static PostDetailResponseDto of(PostEntity postEntity) {
        var postTags = postEntity.getPostTags();
        var tags = new ArrayList<String>();
        for (var postTag : postTags) {
            tags.add(postTag.getTag().getTagString());
        }

        var images = new ArrayList<String>();
        for (var postImage : postEntity.getImages()) {
            images.add(postImage.getUrl());
        }

        return PostDetailResponseDto.builder()
                .postId(postEntity.getId())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                .imageUrls(images)
                .username(postEntity.getUser().getUsername())
                .profileImageUrl(postEntity.getUser().getProfileImageUrl())
                .hashtags(tags)
                .createdAt(postEntity.getCreatedAt())
                .modifiedAt(postEntity.getModifiedAt())
                .likeCount(postEntity.getLikeCount())
                .commentCount(postEntity.getCommentCount())
                .build();
    }
}
