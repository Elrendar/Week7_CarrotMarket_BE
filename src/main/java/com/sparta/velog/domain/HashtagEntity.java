package com.sparta.velog.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "tags")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class HashtagEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long id;
    @Column
    @NotBlank
    private String tag;

    // 이 태그가 달린 게시글들
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTagEntity> postTags;
}
