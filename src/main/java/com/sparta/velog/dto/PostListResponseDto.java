package com.sparta.velog.dto;

import com.sparta.velog.domain.PostEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostListResponseDto {
    @NotBlank
    Long postId;
    @NotBlank
    String title;
    @NotBlank
    String content;
    @NotBlank
    String thumbnailUrl;
    @NotBlank
    String username;
    @NotBlank
    String profileImageUrl;
    int likeCount;

    public static PostListResponseDto of(PostEntity postEntity) {
        var firstImage = "";
        if (postEntity.getImages() != null) {
            if (postEntity.getImages().size() > 0) {
                firstImage = postEntity.getImages().get(0).getUrl();
            }
        }

        return PostListResponseDto.builder()
                .postId(postEntity.getId())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                .thumbnailUrl(firstImage)
                .username(postEntity.getUser().getUsername())
                .profileImageUrl(postEntity.getUser().getProfileImageUrl())
                .likeCount(postEntity.getLikeCount())
                .build();
    }
}
