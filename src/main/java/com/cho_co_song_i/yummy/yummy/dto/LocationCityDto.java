package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCityDto {
    private Long locationCityCode;
    private Long locationCountyCode;
    private String locationCity;
//    private Date regDt;
//    private String regId;
//    private Date chgDt;
//    private String chgId;
}
