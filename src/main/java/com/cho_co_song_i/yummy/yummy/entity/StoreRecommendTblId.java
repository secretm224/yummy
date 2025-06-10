package com.cho_co_song_i.yummy.yummy.entity;

import com.querydsl.core.annotations.QueryEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@QueryEmbeddable
@Getter
public class StoreRecommendTblId {
    @Column(name = "recommend_seq")
    private Long recommendSeq;

    @Column(name = "seq")
    private Long seq;
}
