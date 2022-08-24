package com.sparta.velog.dto;

import com.sparta.velog.domain.PostEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotBlank
    String imageUrl;
    @NotBlank
    String username;
    // 작성자 프로필 이미지 url
    @NotBlank
    String profileImageUrl;
    // 게시글 해쉬태그
    @NotNull
    List<String> hashtags;
    // 게시글 좋아요 수
    int likeCount;
    // 이전 글 id
    Long previousPostId;
    // 다음 글 id
    Long nextPostId;

    public static PostDetailResponseDto of(PostEntity postEntity) {
        var postTags = postEntity.getPostTags();
        var hashtags = new ArrayList<String>();
        for (var postTag : postTags) {
            hashtags.add(postTag.getTag().getTagString());
        }
        return PostDetailResponseDto.builder()
                .postId(postEntity.getId())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                .imageUrl(postEntity.getImageUrl())
                .username(postEntity.getUser().getUsername())
                .profileImageUrl(postEntity.getUser().getProfileImageUrl())
                .hashtags(hashtags)
                .likeCount(postEntity.getLikeCount())
                .build();
    }
}
