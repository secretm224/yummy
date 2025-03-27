package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "store_type_link_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreTypeLinkTbl {

    @EmbeddedId
    private StoreTypeLinkTblId id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq", referencedColumnName = "seq", insertable = false, updatable = false)
    private Store store;
}
