package com.cho_co_song_i.yummy.yummy.dto.search;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalSearchDto {
    private List<StoreSearchDto> storeSearchDtoList;
    private List<SubwayInfoDto> subwayInfoDtoList;
}
