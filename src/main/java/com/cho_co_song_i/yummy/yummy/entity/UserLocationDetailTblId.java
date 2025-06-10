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
public class UserLocationDetailTblId {
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "addr_type", length = 2)
    private String addrType;
}
