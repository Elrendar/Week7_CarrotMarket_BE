package com.sparta.velog.dto;

import com.sparta.velog.domain.CommentEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.validation.constraints.NotBlank;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EnableJpaRepositories
public class CommentResponseDto {
    @NotBlank
    Long postId;
    @NotBlank
    Long userId;
    @NotBlank
    private String comment;

    private CommentResponseDto of(CommentEntity commentEntity){
        return CommentResponseDto.builder()
                .comment(commentEntity.getComment())
                .build();

    }
}
