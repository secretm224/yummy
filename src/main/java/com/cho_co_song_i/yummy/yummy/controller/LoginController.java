package com.cho_co_song_i.yummy.yummy.controller;
import com.cho_co_song_i.yummy.yummy.dto.LoginDto;
import com.cho_co_song_i.yummy.yummy.model.KakaoToken;
import com.cho_co_song_i.yummy.yummy.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.ResponseBody;



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
         if(!code.isEmpty()){
            KakaoToken _kakaoToken = loginService.GetKakaoToken(code);

         }

         Map<String,Object> responseBody = new HashMap<>();
//         responseBody.put("kakao_access_token","");
//         responseBody.put("kakao_payload","");

         return ResponseEntity.ok(responseBody);
     }
}
