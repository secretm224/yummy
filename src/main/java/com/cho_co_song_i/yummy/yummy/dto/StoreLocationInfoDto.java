package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationInfoDto {
    private Long seq;
    private BigDecimal lat;
    private BigDecimal lng;
    private String locationCity;
    private String locationCounty;
    private String locationDistrict;
    private String address;
    private Date regDt;
    private String regId;
    private Date chgDt;
    private String chgId;
}