package com.cho_co_song_i.yummy.yummy.serviceImpl;

import com.cho_co_song_i.yummy.yummy.component.JwtProvider;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthLoginDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.OauthUserSimpleInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.oauth.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.enums.OauthChannelStatus;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.LoginService;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverLoginServiceImpl implements LoginService {
    public OauthChannelStatus getOauthChannel() {
        return OauthChannelStatus.naver;
    }
    public UserOAuthResponse getOauthLoginInfo(String code) throws Exception {
        return null;
    }
    public void saveOauthTokenToRedis(Long userNo, UserOAuthResponse response) {}
    public OauthUserSimpleInfoDto getUserInfosByOauth(Long userNo) {
        return null;
    }
}
