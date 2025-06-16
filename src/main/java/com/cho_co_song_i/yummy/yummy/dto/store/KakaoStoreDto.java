package com.cho_co_song_i.yummy.yummy.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoStoreDto {
    private String addressName;
    private String categoryGroupCode;
    private String categoryGroupName;
    private String categoryName;
    private String phone;
    private String placeUrl;
    private String roadAddressName;
    private String placeName;
    private BigDecimal lat;
    private BigDecimal lng;
}
