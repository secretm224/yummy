package com.cho_co_song_i.yummy.yummy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteDto {
    private String name;
    @JsonProperty("name_chosung")
    private String nameChosung;
}
