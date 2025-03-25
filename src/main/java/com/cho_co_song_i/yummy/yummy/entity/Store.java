package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long seq;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "use_yn", length = 1, nullable = true, columnDefinition = "char(1) default 'Y'")
    private String useYn;

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