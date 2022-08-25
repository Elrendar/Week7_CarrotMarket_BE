package com.sparta.velog.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EnableJpaRepositories
public class PostResposeSearchDto {

    @NotBlank
    Long userId;
    @NotBlank
    Long postId;
    @NotBlank
    String title;
    @NotBlank
    String content;
    //이미지 추가
    @NotBlank
    private String profileImageUrl;
    int likeCount;

    List<String> tags;

    
}
