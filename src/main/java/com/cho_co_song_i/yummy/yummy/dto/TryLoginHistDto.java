package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TryLoginHistDto {
    @Builder.Default
    private LocalDateTime now = LocalDateTime.now();
    private String tryIpAddr;
}
