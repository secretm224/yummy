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
public class UserPictureTblId {
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "channel", length = 10)
    private String channel;
}
