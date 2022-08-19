package com.sparta.velog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "posts")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long postId;
    //유저 정보 가져올것들
    @Column
    private String img;

    @Column(nullable = false)
    private String username;
    //포스트
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;


    @Column
    @NotBlank
    @JsonIgnore
    private final String authority = "ROLE_USER";
}
