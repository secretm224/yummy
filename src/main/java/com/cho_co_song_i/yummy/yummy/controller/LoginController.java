package com.cho_co_song_i.yummy.yummy.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cho_co_song_i.yummy.yummy.dto.LoginDto;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.cho_co_song_i.yummy.yummy.service.LoginService;
import com.fasterxml.jackson.databind.JsonNode;
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
                                                 @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
                                                 @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
                                                 HttpServletResponse res)
     {
         Map<String,Object> responseBody = new HashMap<>();
         if(access_token.isEmpty() && !accessTokenCookie.isEmpty()){
             access_token = accessTokenCookie;
         }

         boolean is_access_token = loginService.CheckKakaoTokens(access_token);
         if(is_access_token){
             JsonNode user_info = loginService.GetKaKaoUser(access_token);
             if(user_info != null){
                   String nick_name = user_info.get("nickname").asText();
                   String picture = user_info.get("picture").asText();
//                 ObjectMapper om = new ObjectMapper();
                 //responseBody = om.convertValue(user_info, new TypeReference<Map<String, Object>>() {});
                 responseBody.put("access_token",access_token);
                 responseBody.put("nickname",nick_name);
                 responseBody.put("picture",picture);

             }
         }else{
//             reflesh token
             JsonNode token_info = loginService.GetAccessTokenByRefreshToken(refreshTokenCookie);
             if(token_info != null){
                 String n_access_token = token_info.get("access_token").asText();
                 String n_refresh_token = token_info.get("refresh_token").asText();
                 String n_id_token = token_info.get("id_token").asText();

                 if(!n_access_token.isEmpty()){
                     Cookie accessCookie  = new Cookie("accessToken",n_access_token);
                     accessCookie.setHttpOnly(true);
                     accessCookie.setSecure(false);
                     accessCookie.setPath("/");
                     res.addCookie(accessCookie);
                 }

                 if(!n_refresh_token.isEmpty()){
                     Cookie refreshCookie = new Cookie("refreshToken",n_refresh_token);
                     refreshCookie.setHttpOnly(true);
                     refreshCookie.setSecure(false);
                     refreshCookie.setPath("/");
                     res.addCookie(refreshCookie);
                 }

                 if(!n_id_token.isEmpty()){
                     DecodedJWT _decodejwt = JWT.decode(n_id_token);
                     Map<String,Object> payload = _decodejwt.getClaims().entrySet().stream()
                                                  .collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().as(Object.class)));

                     if(payload != null)
                     {
                        String nick_name = payload.get("nickname").toString();
                        String picture = payload.get("picture").toString();

                         responseBody.put("access_token",access_token);
                         responseBody.put("nickname",nick_name);
                         responseBody.put("picture",picture);
                     }
                 }
             }
         }

         return ResponseEntity.ok(responseBody);
     }


}
