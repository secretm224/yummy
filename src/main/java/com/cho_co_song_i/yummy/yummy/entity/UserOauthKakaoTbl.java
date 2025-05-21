package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user_oauth_kakao_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOauthKakaoTbl {

    @Id
    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "token_id", length = 255,  nullable = false)
    private String tokenId;

    @Column(name = "oauth_banned_yn", length = 1, nullable = false, columnDefinition = "char(1) default 'N'")
    private Character oauthBannedYn;

    @Column(name="reg_dt",nullable = false)
    private Date reg_dt;

    @Column(name="reg_id",length = 25,nullable = false)
    private String reg_id;

    @Column(name="chg_dt", nullable = true)
    private Date chg_dt;

    @Column(name="chg_id",length = 25, nullable = true)
    private String chg_id;

}
