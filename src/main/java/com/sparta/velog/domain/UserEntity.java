package com.sparta.velog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparta.velog.dto.UserInfoUpdateDto;
import com.sparta.velog.dto.UserRequestDto;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class UserEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @Column
    @NotBlank
    private String username;
    @Column
    @NotBlank
    @JsonIgnore
    private String password;
    @Column
    private String profileImageUrl;
    @Column
    @NotNull
    private String selfDescription;
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
                .selfDescription("")
                .build();
    }

    public void updateInfo(UserInfoUpdateDto requestDto) {
        if (requestDto.getProfileImageUrl() != null) {
            this.profileImageUrl = requestDto.getProfileImageUrl();
        }
        if (requestDto.getSelfDescription() != null) {
            this.selfDescription = requestDto.getSelfDescription();
        }
    }
}
