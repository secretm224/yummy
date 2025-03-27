package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "store_recommend_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRecommendTbl {
    @EmbeddedId
    private StoreRecommendTblId id;

    @Column(name = "recommend_end_dt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date recommendEndDt;

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

}
