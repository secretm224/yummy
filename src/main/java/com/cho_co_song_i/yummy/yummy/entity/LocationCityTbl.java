package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "location_city_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCityTbl {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "location_city_code")
//    private Long locationCityCode;
//
//    @Id
//    @Column(name = "location_county_code", nullable = false)
//    private Long locationCountyCode;

    @EmbeddedId
    private LocationCityTblId id;

    @Column(name = "location_city", nullable = false, length = 25)
    private String locationCity;

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
    @JoinColumn(name = "location_county_code", insertable = false, updatable = false)
    private LocationCountyTbl locationCounty;
}
