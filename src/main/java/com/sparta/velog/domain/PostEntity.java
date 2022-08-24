package com.sparta.velog.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class PostEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    // 포스트
    @Setter
    @Column(nullable = false)
    private String title;
    @Setter
    @Column(nullable = false)
    private String content;

    // 이미지 추가
    @Setter
    @Column(nullable = false)
    private String imageUrl;

    // 이 게시물에 달린 태그들
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTagEntity> postTags = new ArrayList<>();

    // 좋아요를 누른 유저 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeEntity> likePosts = new ArrayList<>();

    @Column(nullable = false)
    private int likeCount = 0;

    // 글쓴이
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "user_id", updatable = false, insertable = false)
    private Long userId;

    public int addLikeCount() {
        return ++likeCount;
    }

    public int minusLikeCount() {
        return --likeCount;
    }
}
