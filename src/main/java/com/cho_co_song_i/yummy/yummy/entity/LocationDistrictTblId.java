package com.cho_co_song_i.yummy.yummy.entity;

import com.querydsl.core.annotations.QueryEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@QueryEmbeddable
public class LocationDistrictTblId {
    @Column(name = "location_district_code")
    private Long locationDistrictCode;

    @Column(name = "location_city_code")
    private Long locationCityCode;

    @Column(name = "location_county_code")
    private Long locationCountyCode;
}
