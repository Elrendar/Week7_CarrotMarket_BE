package com.sparta.velog.dto;

import com.sparta.velog.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.springframework.util.Assert;

import javax.validation.constraints.NotBlank;

@Jacksonized
@Getter
@Builder(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserResponseDto {
    @NotBlank
    String username;
    String profileImageUrl;
    String selfDescription;
    String myVelogName;

    public static UserResponseDto of(UserEntity userEntity) {
        Assert.notNull(userEntity, "userEntity가 비어있습니다.");
        return UserResponseDto.builder()
                .username(userEntity.getUsername())
                .selfDescription(userEntity.getSelfDescription())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .myVelogName(userEntity.getMyVelogName())
                .build();
    }
}
