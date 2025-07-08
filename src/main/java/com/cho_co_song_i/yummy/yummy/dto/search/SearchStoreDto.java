package com.cho_co_song_i.yummy.yummy.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchStoreDto {
    private String address;
    private String lat;
    private String lng;

    @JsonProperty("location_city")
    private String locationCity;
    @JsonProperty("location_county")
    private String locationCounty;
    @JsonProperty("location_district")
    private String locationDistrict;
    @JsonProperty("major_type")
    private List<Integer> majorType;
    private String name;
    @JsonProperty("recommend_names")
    private List<String> recommendNames;
    private int seq;
    @JsonProperty("sub_type")
    private List<Integer> subType;
    private ZonedDateTime timestamp;
    private String type;
    @JsonProperty("zero_possible")
    private boolean zeroPossible;
    private String tel;
    private String url;
    @JsonProperty("category_icon")
    private String categoryIcon;
}
