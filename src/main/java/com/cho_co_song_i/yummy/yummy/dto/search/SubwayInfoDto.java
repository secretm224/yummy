package com.cho_co_song_i.yummy.yummy.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubwayInfoDto {
    private int seq;

    @JsonProperty("subway_line")
    private String subwayLine;

    @JsonProperty("station_name")
    private String stationName;

    @JsonProperty("station_eng_name")
    private String stationEngName;

    private String lat;
    private String lng;

    @JsonProperty("station_load_addr")
    private String stationLoadAddr;
}