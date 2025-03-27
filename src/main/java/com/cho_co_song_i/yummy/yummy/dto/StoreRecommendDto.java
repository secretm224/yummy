package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRecommendDto {
    private Long recommendSeq;
    private Long seq;
    private Date recommendEndDt;
    private Date regDt;
    private String regId;
    private Date chgDt;
    private String chgId;
}
