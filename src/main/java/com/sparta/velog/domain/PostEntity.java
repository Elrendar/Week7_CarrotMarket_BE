package com.sparta.velog.domain;

import com.sparta.velog.dto.PostRequestDto;
import lombok.*;

import javax.persistence.*;
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
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;

    // 이 게시물에 달린 태그들
    @Setter
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTagEntity> postTags;

    // 좋아요를 누른 유저 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeEntity> likePosts;

    @Column(nullable = false)
    private int likeCount = 0;

    // 글쓴이
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "user_id", updatable = false, insertable = false)
    private Long userId;

    public PostEntity update(PostRequestDto postRequestDto) {
        if (postRequestDto.getTitle() != null) {
            this.title = postRequestDto.getTitle();
        }
        if (postRequestDto.getContent() != null) {
            this.content = postRequestDto.getContent();
        }
        return this;
    }

    public int addLikeCount() {
        return ++likeCount;
    }

    public int minusLikeCount() {
        return --likeCount;
    }
}
