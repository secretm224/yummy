package com.cho_co_song_i.yummy.yummy.dto.oauth.kakao;

import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KakaoOauthInfoDto {
    private KakaoToken kakaoToken;
    private OauthUserSimpleInfoDto oauthUserSimpleInfoDto;
}
