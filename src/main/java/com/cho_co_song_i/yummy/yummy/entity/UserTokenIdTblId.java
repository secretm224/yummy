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
public class UserTokenIdTblId {
    @Column(name = "token_id", nullable = false, columnDefinition = "char(36)")
    private String tokenId;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    public UserTokenIdTblId(String tokenId, Long userNo) {
        this.tokenId = tokenId;
        this.userNo = userNo;
    }
}