package com.cho_co_song_i.yummy.yummy.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user_temp_pw_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTempPwTbl {
    @Id
    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

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

    @Transient
    private boolean isNew = true;

    public Long getId(){
        return this.userNo;
    }

    public boolean isNew(){
        return this.userNo == null || isNew;
    }

    public void markNotNew(){
        this.isNew = false;
    }

    @MapsId("userNo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private UserTbl user;
}
