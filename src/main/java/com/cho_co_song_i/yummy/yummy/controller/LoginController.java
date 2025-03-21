package com.cho_co_song_i.yummy.yummy.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cho_co_song_i.yummy.yummy.dto.LoginDto;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.cho_co_song_i.yummy.yummy.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService){
        this.loginService = loginService;

    }
    @GetMapping
    public String Login(Model model) {
        model.addAttribute("title", "로그인페이지");
        return "login";
    }

    @PostMapping("/kakao/callback")
    @ResponseBody
    public ResponseEntity<?> KaKaoCode(@RequestBody LoginDto loginDto, HttpServletResponse res , HttpServletRequest req){

        String code = loginDto.getCode();
        Map<String,Object> responseBody = new HashMap<>();


        if(!code.isEmpty()){
            KakaoToken _kakaoToken = loginService.GetKakaoToken(code);
            if(_kakaoToken != null){
                String access_token = _kakaoToken.getAccess_token();
                String refresh_token = _kakaoToken.getRefresh_token();
                String id_token = _kakaoToken.getId_token();

                if(!access_token.isEmpty()){
                    Cookie accessCookie  = new Cookie("accessToken",access_token);
                    accessCookie.setHttpOnly(true);
                    accessCookie.setSecure(false);
                    accessCookie.setPath("/");
                    res.addCookie(accessCookie);
                }

                if(!refresh_token.isEmpty()){
                    Cookie refreshCookie = new Cookie("refreshToken",refresh_token);
                    refreshCookie.setHttpOnly(true);
                    refreshCookie.setSecure(false);
                    refreshCookie.setPath("/");
                    res.addCookie(refreshCookie);
                }

                DecodedJWT _decodejwt = JWT.decode(id_token);
                Map<String,Object> payload = _decodejwt.getClaims().entrySet().stream()
                                             .collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().as(Object.class)));

                responseBody.put("kakao_access_token",access_token);
                responseBody.put("kakao_payload",payload);
            }
         }else{
            responseBody.put("kakao_access_token",null);
            responseBody.put("kakao_payload",null);
        }

         return ResponseEntity.ok(responseBody);
     }


     @PostMapping("/kakao/GetUserInfoByToken")
     public ResponseEntity<?> GetUserinfoByToken(@RequestBody String access_token ,
                                                 @CookieValue(value = "access_token", required = false) String accessTokenCookie)
     {
         Map<String,Object> responseBody = new HashMap<>();
         if(access_token.isEmpty() && !accessTokenCookie.isEmpty()){
             access_token = accessTokenCookie;
         }

         boolean is_access_token = loginService.CheckKakaoTokens(access_token);
         if(is_access_token){

         }

         return ResponseEntity.ok(responseBody);
     }


}
