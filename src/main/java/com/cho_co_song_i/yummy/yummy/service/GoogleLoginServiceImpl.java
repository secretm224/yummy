package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import com.cho_co_song_i.yummy.yummy.dto.UserOAuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleLoginServiceImpl implements LoginService {
    @Override
    public UserOAuthResponse handleOAuthLogin(String code, HttpServletResponse res) {
        return null;
    }
}
