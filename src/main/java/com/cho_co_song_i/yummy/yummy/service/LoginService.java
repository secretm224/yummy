package com.cho_co_song_i.yummy.yummy.service;

import com.cho_co_song_i.yummy.yummy.dto.UserOAuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {

    UserOAuthResponse handleOAuthLogin(String code, HttpServletResponse res) throws Exception;


}
