package com.cho_co_song_i.yummy.yummy.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationDto {
    private Long seq;
    private String name;
    private BigDecimal lat;
    private BigDecimal lng;
}
