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
public class TagEntity extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;
    @Column
    @NotBlank
    private String tagString;

    // 이 태그가 달린 게시글들
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTagEntity> postTags;
}
