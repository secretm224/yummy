package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "user_token_id_tbl")
@NoArgsConstructor
@Getter
public class UserTokenIdTbl implements Persistable<UserTokenIdTblId>{

    @EmbeddedId
    private UserTokenIdTblId id;

    @Column(name = "reg_dt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date regDt;

    @Column(name = "reg_id", nullable = false, length = 25)
    private String regId;

    @Column(name = "chg_dt", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date chgDt;

    @Column(name = "chg_id", nullable = true, length = 25)
    private String chgId;


    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public UserTokenIdTblId getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return isNew || this.id == null;
    }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() {
        this.isNew = true;
    }

    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserTbl user;

    public UserTokenIdTbl(UserTbl user, String tokenId, String regId) {
        Instant nowInstant = Instant.now();

        UserTokenIdTblId userTokenIdTblId = new UserTokenIdTblId(tokenId, user.getUserNo());
        this.user = user;
        this.id = userTokenIdTblId;
        this.regId = regId;
        this.regDt = Date.from(nowInstant);
    }

}
