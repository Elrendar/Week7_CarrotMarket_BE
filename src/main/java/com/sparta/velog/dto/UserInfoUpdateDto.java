package com.sparta.velog.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.springframework.web.multipart.MultipartFile;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserInfoUpdateDto {
    // 프로필 사진 추가
    private MultipartFile profileImage;
    String selfDescription;
    String myVelogName;
}
