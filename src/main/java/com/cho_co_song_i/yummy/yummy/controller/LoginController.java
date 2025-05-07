package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.*;


import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    private final LoginService kakoLoginService;
    private final LoginService naverLoginService;
    private final LoginService googleLoginService;
    private final YummyLoginService yummyLoginService;

    public LoginController(KakaoLoginServiceImpl kakoLoginService, NaverLoginServiceImpl naverLoginService,
                           GoogleLoginServiceImpl googleLoginService, YummyLoginService yummyLoginService){
        this.kakoLoginService = kakoLoginService;
        this.naverLoginService = naverLoginService;
        this.googleLoginService = googleLoginService;
        this.yummyLoginService = yummyLoginService;
    }

    @GetMapping
    public String Login(Model model) {
        model.addAttribute("title", "로그인페이지");
        return "login";
    }

    /**
     * [Test Code]
     * @param text
     * @return
     */
    @GetMapping("/hashtest")
    public ResponseEntity<?> HashTest(
            @RequestParam(value = "text", required = false) String text
    ) {

        try {

            String saltValue = HashUtil.generateSalt();
            String hashVal = HashUtil.hashWithSalt(text, saltValue);

            return ResponseEntity.ok(hashVal);

        } catch(Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * 기본적으로 아이디/비밀번호 입력하고 로그인 시도하는 경우 처리해주는 함수.
     * @param standardLoginDto
     * @param res
     * @param req
     * @return
     */
    @PostMapping("/standardLogin")
    public ResponseEntity<PublicStatus> StandardLogin(@RequestBody StandardLoginDto standardLoginDto, HttpServletResponse res) {
        try {
            return ResponseEntity.ok(yummyLoginService.standardLoginUser(standardLoginDto, res));
        } catch(Exception e) {
            log.error("[Error][LoginController->StandardLogin] {}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.AUTH_ERROR);
        }
    }

    /**
     * 로그아웃을 해주는 함수
     * @param res
     * @return
     */
    @PostMapping("/standardLogout")
    public ResponseEntity<Void> StandardLogout(HttpServletResponse res) {
        yummyLoginService.standardLogoutUser(res);
        return ResponseEntity.noContent().build();
    }



    /**
     * 로그인을 체크해주는 함수
     * @param res
     * @param req
     * @return
     */
    @PostMapping("/auth/loginCheck")
    @ResponseBody
    public ResponseEntity<ServiceResponse<Optional<UserBasicInfoDto>>> LoginCheck(HttpServletResponse res , HttpServletRequest req) {
        /* 로그인 체크 처리 */
        ServiceResponse<Optional<UserBasicInfoDto>> result = yummyLoginService.checkLoginUser(res, req);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/oauth2/kakao")
    @ResponseBody
    public ResponseEntity<PublicStatus> KakaoLogin(@RequestBody OauthLoginDto loginDto, HttpServletResponse res , HttpServletRequest req) {

        try {

            UserOAuthResponse result = kakoLoginService.handleOAuthLogin(loginDto.getCode());

            if (result.getPublicStatus() == PublicStatus.SUCCESS) {
                /* Oauth2 인증 성공해서 유저 정보가 있는 경우 */
                return ResponseEntity.ok(yummyLoginService.oauthLogin(result.getUserNum(), result.getIdToken(), res));
            }
            else if (result.getPublicStatus() == PublicStatus.JOIN_TARGET_MEMBER) {
                /*
                * 유저에게 신규 가입 또는 기존회원 연동 하게 시킴.
                * -> 임시 jwt 토큰 발급
                */
                kakoLoginService.generateTempOauthJwtCookie(result.getIdToken(), res);
                return ResponseEntity.ok(PublicStatus.JOIN_TARGET_MEMBER);
            }

            return ResponseEntity.ok(PublicStatus.CASE_ERR);
        } catch(Exception e) {
            log.error("[Error][LoginController->KakaoLogin] {}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.SERVER_ERR);
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public String Test() {
        return "Call Method Test";
    }

    @GetMapping("/deploytest")
    @ResponseBody
    public String deployTest(){
        return "배포 테스트";
    }
}
