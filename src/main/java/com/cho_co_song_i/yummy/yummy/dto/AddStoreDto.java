package com.cho_co_song_i.yummy.yummy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStoreDto {
    @JsonProperty("name")
    private String name;
    @JsonProperty("lat")
    private BigDecimal lat;
    @JsonProperty("lng")
    private BigDecimal lng;

    @JsonProperty("type")
    private String type;

    @JsonProperty("is_beefulpay")
    private Boolean isBeefulPay;

    @JsonProperty("location_county")
    private String locationCounty;

    @JsonProperty("location_city")
    private String locationCity;

    @JsonProperty("location_district")
    private String locationDistrict;

    @JsonProperty("sub_type")
    private int subType;
    @JsonProperty("address")
    private String address;

    @JsonProperty("tel")
    private String tel;

    @JsonProperty("url")
    private String url;
}
