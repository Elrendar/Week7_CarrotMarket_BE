package com.sparta.velog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparta.velog.dto.UserRequestDto;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
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
    @NotNull
    private String myVelogName;
    @Column
    @NotBlank
    @JsonIgnore
    private final String authority = "ROLE_USER";

    // 작성글 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> posts;

    // 좋아요를 누른 글 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeEntity> likePosts;

    // 작성글 사진 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImageEntity> postImages = new ArrayList<>();

    public static UserEntity of(UserRequestDto userRequestDto, PasswordEncoder passwordEncoder) {
        return UserEntity.builder()
                .username(userRequestDto.getUsername())
                .password(passwordEncoder.encode(userRequestDto.getPassword1()))
                .profileImageUrl("https://velog.velcdn.com/images/hyexjun/profile/108c8f1a-b604-4881-9906-00270be78272/image.jpg")
                .selfDescription("")
                .myVelogName(userRequestDto.getUsername() + "의 벨로그")
                .build();
    }

    public void updateInfo(String profileImageUrl, String selfDescription, String myVelogName) {
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }

        if (selfDescription != null) {
            this.selfDescription = selfDescription;
        }

        if (myVelogName != null) {
            this.myVelogName = myVelogName;
        }
    }
}
