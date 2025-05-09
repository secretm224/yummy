package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.Date;



@Entity
@Table(name = "user_location_detail_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationDetailTbl implements Persistable<UserLocationDetailTblId> {

    @EmbeddedId
    private UserLocationDetailTblId id;

    @Column(name = "addr", length = 255, nullable = false)
    private String addr;

    @Column(name = "lng", precision = 10, scale = 7, nullable = false)
    private BigDecimal lng;

    @Column(name = "lat", precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    @Column(name = "reg_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date regDt;

    @Column(name = "reg_id", length = 25)
    private String regId;

    @Column(name = "chg_dt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chgDt;

    @Column(name = "chg_id", length = 25)
    private String chgId;

    /* ✅ Hibernate 에게 신규 엔티티임을 알려주기 위해 사용 */
    @Transient
    private boolean isNew = false;

    @Override
    public UserLocationDetailTblId getId() { return this.id; }

    @Override
    public boolean isNew() { return isNew || this.id == null; }

    /* ✅ 새로운 엔티티를 표시하는 메서드 */
    public void markAsNew() { this.isNew = true; }


    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserTbl user;
}
