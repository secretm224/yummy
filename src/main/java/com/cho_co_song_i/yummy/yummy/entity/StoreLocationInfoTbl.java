package com.cho_co_song_i.yummy.yummy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "store_location_info_tbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationInfoTbl {

    @Id
    @Column(name = "seq")
    private Long seq;

    @Column(name = "lat", precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    @Column(name = "lng", precision = 10, scale = 7, nullable = false)
    private BigDecimal lng;

    @Column(name = "location_city", nullable = true, length = 25)
    private String locationCity;

    @Column(name = "location_county", nullable = true, length = 25)
    private String locationCounty;

    @Column(name = "location_district", nullable = true, length = 25)
    private String locationDistrict;

    @Column(name = "address", nullable = true, length = 500)
    private String address;

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

    @OneToOne(mappedBy = "storeLocations", fetch = FetchType.LAZY)
    private Store store;

}
