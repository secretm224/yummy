package com.cho_co_song_i.yummy.yummy.entity;

import com.querydsl.core.annotations.QueryEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@QueryEmbeddable
public class UserAuthTblId {
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "login_channel", length = 10)
    private String loginChannel;

    @Column(name = "token_id", length = 255)
    private String tokenId;
}
