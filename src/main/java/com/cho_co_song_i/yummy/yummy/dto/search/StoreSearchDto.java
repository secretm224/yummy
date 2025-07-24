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
public class StoreSearchDto {

    private String address;
    private String lat;
    private String lng;
    private String name;

    @JsonProperty("road_address")
    private String roadAddress;
    private int seq;
    private String tel;
    private String url;

    @JsonProperty("zero_possible")
    private boolean zeroPossible;
}
