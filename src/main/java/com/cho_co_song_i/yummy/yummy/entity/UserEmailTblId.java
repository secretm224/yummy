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
public class UserEmailTblId {
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "user_email_address", nullable = false, length = 255)
    private String userEmailAddress;
}
