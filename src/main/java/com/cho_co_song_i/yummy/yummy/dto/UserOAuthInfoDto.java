package com.cho_co_song_i.yummy.yummy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOAuthInfoDto {
    private String userTokenId;
    private String nickName;
    private String userPicture;
    private Boolean isJoinSiteYn;
}