package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "zero_possible_market")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZeroPossibleMarket {
    @Id
    @Column(name = "seq")
    private Long seq;

    @Column(name = "use_yn", nullable = true, columnDefinition = "char(1)")
    private Character useYn;

    @Column(name = "name", nullable = true, length = 255)
    private String name;

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

    @OneToOne(mappedBy = "zeroPossibles", fetch = FetchType.LAZY)
    private Store store;
}
