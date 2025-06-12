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
@Table(name = "user_phone_number_tbl")
@NoArgsConstructor
@Getter
public class UserPhoneNumberTbl implements Persistable<UserPhoneNumberTblId>  {
    @EmbeddedId
    private UserPhoneNumberTblId id;

    @Column(name = "telecom_name", nullable = false, length = 25)
    private String telecomName;

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
    public UserPhoneNumberTblId getId() { return this.id; }

    @Override
    public boolean isNew() { return isNew || this.id == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }

    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserTbl user;

    public UserPhoneNumberTbl(UserTbl user, String phoneNumber, String telecom, String regId) {
        Instant nowInstant = Instant.now();
        this.id = new UserPhoneNumberTblId(user.getUserNo(), phoneNumber);
        this.user = user;
        this.telecomName = telecom;
        this.regDt = Date.from(nowInstant);
        this.regId = regId;
    }
}
