package com.cho_co_song_i.yummy.yummy.dto.oauth.google;

import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleOauthInfoDto {
    private GoogleToken googleToken;
    private OauthUserSimpleInfoDto oauthUserSimpleInfoDto;
}
