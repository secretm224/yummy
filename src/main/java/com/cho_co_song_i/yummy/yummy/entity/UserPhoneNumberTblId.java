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
public class UserPhoneNumberTblId {
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "phone_number", columnDefinition = "CHAR(11)")
    private String phoneNumber;

    public UserPhoneNumberTblId(Long userNo, String phoneNumber) {
        this.userNo = userNo;
        this.phoneNumber = phoneNumber;
    }
}
