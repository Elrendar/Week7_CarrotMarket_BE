package com.sparta.velog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparta.velog.dto.UserInfoUpdateDto;
import com.sparta.velog.dto.UserRequestDto;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column
    @NotBlank
    String username;
    @Column
    @NotBlank
    @JsonIgnore
    String password;
    @Column
    @NotBlank
    @JsonIgnore
    private final String authority = "ROLE_USER";
    @Column
    String profileImageUrl;
    @Column
    String description;

    public static UserEntity of(UserRequestDto userRequestDto, PasswordEncoder passwordEncoder) {
        return UserEntity.builder()
                .username(userRequestDto.getUsername())
                .password(passwordEncoder.encode(userRequestDto.getPassword1()))
                .build();
    }

    public void updateInfo(UserInfoUpdateDto requestDto) {
        if (requestDto.getProfileImageUrl() != null) {
            this.profileImageUrl = requestDto.getProfileImageUrl();
        }
        if (requestDto.getDescription() != null) {
            this.profileImageUrl = requestDto.getDescription();
        }
    }
}
