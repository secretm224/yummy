package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.*;


import com.cho_co_song_i.yummy.yummy.serviceImpl.GoogleLoginServiceImpl;
import com.cho_co_song_i.yummy.yummy.serviceImpl.KakaoLoginServiceImpl;
import com.cho_co_song_i.yummy.yummy.serviceImpl.NaverLoginServiceImpl;
import com.cho_co_song_i.yummy.yummy.utils.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/login")
@Slf4j
@RequiredArgsConstructor
public class LoginController {
    private final KakaoLoginServiceImpl kakoLoginService;
    private final NaverLoginServiceImpl naverLoginService;
    private final GoogleLoginServiceImpl googleLoginService;
    private final YummyLoginService yummyLoginService;

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
     * @throws Exception
     */
    @PostMapping("/standardLogin")
    public ResponseEntity<PublicStatus> standardLogin(@RequestBody StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) throws Exception {
        return ResponseEntity.ok(yummyLoginService.standardLoginUser(standardLoginDto, res, req));
    }

    /**
     * 로그아웃을 해주는 함수
     * @param res
     * @return
     */
    @PostMapping("/standardLogout")
    public ResponseEntity<Void> standardLogout(HttpServletResponse res) {
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
    public ResponseEntity<ServiceResponse<Optional<UserBasicInfoDto>>> verifyLoginUser(HttpServletResponse res , HttpServletRequest req) throws Exception {
        /* 로그인 체크 처리 */
        ServiceResponse<Optional<UserBasicInfoDto>> result = yummyLoginService.verifyLoginUser(res, req);
        return ResponseEntity.ok(result);
    }

    /**
     * Kakao Oauth2 를 통해서 로그인 해주는 컨트롤러
     * @param loginDto
     * @param res
     * @param req
     * @return
     */
    @PostMapping("/oauth2/kakao")
    @ResponseBody
    public PublicStatus kakaoLogin(@RequestBody OauthLoginDto loginDto, HttpServletResponse res , HttpServletRequest req) throws Exception{
        return kakoLoginService.handleOAuthLogin(loginDto, res, req);
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