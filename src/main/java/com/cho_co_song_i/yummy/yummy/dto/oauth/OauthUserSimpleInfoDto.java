package com.cho_co_song_i.yummy.yummy.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OauthUserSimpleInfoDto {
    private String userTokenId;
    private String nickName;
    private String profileImg;
}