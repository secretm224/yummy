package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "user_oauth_kakao_tbl")
@NoArgsConstructor
@Getter
public class UserOauthKakaoTbl implements Persistable<String> {

    @Id
    @Column(name = "token_id", length = 255,  nullable = false)
    private String tokenId;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

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


    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public String getId() { return this.tokenId; }

    @Override
    public boolean isNew() { return isNew || this.tokenId == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }

    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserTbl user;

    public UserOauthKakaoTbl(UserTbl userTbl, String idToken, String regId) {
        Instant nowInstant = Instant.now();
        this.user = userTbl;
        this.userNo = userTbl.getUserNo();
        this.tokenId = idToken;
        this.oauthBannedYn = 'N';
        this.reg_dt = Date.from(nowInstant);
        this.reg_id = regId;
    }

}
