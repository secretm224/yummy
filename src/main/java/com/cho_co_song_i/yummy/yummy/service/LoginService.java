package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.UserOAuthInfoDto;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {

    UserOAuthInfoDto handleOAuthLogin(String code, HttpServletResponse res);


}
