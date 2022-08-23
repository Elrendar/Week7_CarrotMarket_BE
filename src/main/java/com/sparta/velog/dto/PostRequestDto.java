package com.sparta.velog.dto;

import com.sparta.velog.domain.PostEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostRequestDto {
    @NotBlank
    String title;
    @NotBlank
    String content;

    //이미지 추가
    @NotBlank
    private MultipartFile imageFile;

    List<String> tags;
}
