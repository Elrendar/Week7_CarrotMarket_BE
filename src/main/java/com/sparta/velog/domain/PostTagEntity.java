package com.sparta.velog.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "post_tags")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
// Post와 Tag의 다대다 연관관계 매핑을 위한 중간 테이블
public class PostTagEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_tag_id")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @Column(name = "post_id", updatable = false, insertable = false)
    private Long postId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private HashtagEntity hashtag;

    @Column(name = "hashtag_id", updatable = false, insertable = false)
    private Long hashtagId;

    public String getTag() {
        return hashtag.getTag();
    }
}
