package com.sparta.velog.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "comments")
@Entity
public class CommentEntity extends TimeStamp{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentsId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment; // 댓글 내용

    @Column(nullable = false)
    private String username;

    @ManyToOne
    @JoinColumn(name = "posts_id")
    private PostEntity postEntity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity; // 작성자


    public CommentEntity(PostEntity postEntity, String comment) {
        this.postEntity = postEntity;
        this.username = ""; //로그인된 유저 정보 받아오기
        this.comment = comment;
    }
}
