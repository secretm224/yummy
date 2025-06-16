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
public class StoreCategoryTblId {
    @Column(name = "seq")
    private Long seq;

    @Column(name = "category_seq")
    private Long categorySeq;

    public StoreCategoryTblId(Long seq, Long categorySeq) {
        this.seq = seq;
        this.categorySeq = categorySeq;
    }
}
