package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* TEST 용 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDto {
    private Long locationCountyCode;
    private String locationCounty;
    private Long locationCityCode;
    private String locationCity;
}
