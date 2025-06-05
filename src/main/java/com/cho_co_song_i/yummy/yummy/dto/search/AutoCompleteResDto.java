package com.cho_co_song_i.yummy.yummy.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoCompleteResDto {
    private String name;
    private float score;
    private int keywordWeight;
}
