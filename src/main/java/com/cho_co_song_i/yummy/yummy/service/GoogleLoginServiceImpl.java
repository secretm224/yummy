package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthResponse;
import com.cho_co_song_i.yummy.yummy.utils.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleLoginServiceImpl implements LoginService {

    private final JwtProviderService jwtProviderService;

    public GoogleLoginServiceImpl(JwtProviderService jwtProviderService) {
        this.jwtProviderService = jwtProviderService;
    }

    @Override
    public UserOAuthResponse handleOAuthLogin(String code) {
        return null;
    }

    /**
     * 회원이 oauth2 를 통해 기존아이디 통합 또는 회원가입을 위해 임시 jwt 쿠키를 발급해준다.
     * @param idToken
     * @param res
     */
    @Override
    public void generateTempOauthJwtCookie(String idToken, HttpServletResponse res) {
        String jwtToken = jwtProviderService.generateOauthTempToken(idToken, "google");
        CookieUtil.addCookie(res, "yummy-oauth-temp-token", jwtToken, 300);
    }
}
