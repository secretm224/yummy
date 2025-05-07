package com.cho_co_song_i.yummy.yummy.dto;

import com.cho_co_song_i.yummy.yummy.entity.UserTbl;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardLoginBasicResDto {
    private PublicStatus publicStatus;
    private UserTbl userTbl;
    private boolean tempUserYn;
}
