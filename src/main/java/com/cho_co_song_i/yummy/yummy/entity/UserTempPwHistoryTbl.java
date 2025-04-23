package com.cho_co_song_i.yummy.yummy.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user_temp_pw_history_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTempPwHistoryTbl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hist_no")
    private Long histNo;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "temp_pw", nullable = false, columnDefinition = "CHAR(44)")
    private String tempPw;

    @Column(name = "temp_pw_salt", nullable = false, columnDefinition = "CHAR(24)")
    private String tempPwSalt;

    @Column(name = "end_yn", nullable = true, columnDefinition = "CHAR(1)")
    private String endYn;

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
        return this.histNo;
    }

    public boolean isNew(){
        return this.histNo == null || isNew;
    }

    public void markNotNew(){
        this.isNew = false;
    }
}
