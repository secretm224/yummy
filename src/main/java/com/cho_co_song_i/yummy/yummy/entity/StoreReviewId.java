package com.cho_co_song_i.yummy.yummy.entity;

import com.querydsl.core.annotations.QueryEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@QueryEmbeddable
@Getter
public class StoreReviewId {
    @Column(name = "seq", nullable = false)       /* 상점 고유번호 */
    private Long storeSeq;

    @Column(name = "review_id", nullable = false) /* 리뷰 고유번호 */
    private Long reviewId;
}
