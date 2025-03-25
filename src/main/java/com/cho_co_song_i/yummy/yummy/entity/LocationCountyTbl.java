package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "location_county_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCountyTbl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_county_code")
    private Long locationCountyCode;

    @Column(name = "location_county", nullable = false, length = 25)
    private String locationCounty;

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

    /* 관계 설정 */
    @OneToMany(mappedBy = "locationCounty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LocationCityTbl> locationCities = new ArrayList<>();
}
