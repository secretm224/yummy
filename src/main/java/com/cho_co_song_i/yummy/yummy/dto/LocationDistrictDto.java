package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDistrictDto {
    private Long locationDistrictCode;
    private Long locationCityCode;
    private Long locationCountyCode;
    private String locationDistrict;
    //    private Date regDt;
    //    private String regId;
    //    private Date chgDt;
    //    private String chgId;
}