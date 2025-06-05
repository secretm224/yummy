package com.cho_co_song_i.yummy.yummy.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteDto {
    @JsonProperty("keyword_weight")
    private int keywordWeight;
    private String name;
    @JsonProperty("name_chosung")
    private String nameChosung;
}
