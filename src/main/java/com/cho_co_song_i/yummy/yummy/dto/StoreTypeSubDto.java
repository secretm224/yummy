package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreTypeSubDto {
    private Long subType;
    private Long majorType;
    private String typeName;
    //    private Date regDt;
    //    private String regId;
    //    private Date chgDt;
    //    private String chgId;
}
