package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.JoinMemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/joinMember")
@Slf4j
public class JoinMemberController {

    private final JoinMemberService joinMemberService;

    public JoinMemberController(JoinMemberService joinMemberService) {
        this.joinMemberService = joinMemberService;
    }

    /**
     * 회원가입 해주는 컨트롤러 함수
     * @param joinMemberDto
     * @return
     */
    @PostMapping("/join")
    @ResponseBody
    public ResponseEntity<PublicStatus> joinMember(@RequestBody JoinMemberDto joinMemberDto, HttpServletResponse res, HttpServletRequest req) {

        try {
            PublicStatus result = joinMemberService.joinMember(res, req, joinMemberDto);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            log.error("{}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.SERVER_ERR);
        }
    }

    /**
     * 아이디를 찾아주는 컨트롤러
     * @param findIdDto
     * @return
     */
    @PostMapping("/findId")
    @ResponseBody
    public ResponseEntity<PublicStatus> findId(@RequestBody FindIdDto findIdDto) {
        try {
            return ResponseEntity.ok(joinMemberService.findId(findIdDto));
        } catch(Exception e) {
            log.error("[Error][JoinMemberController->findId] {}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.SERVER_ERR);
        }
    }

    /**
     * 비밀번호를 찾아주는 컨트롤러
     * @param findPwDto
     * @return
     */
    @PostMapping("/findPw")
    @ResponseBody
    public ResponseEntity<PublicStatus> findPw(@RequestBody FindPwDto findPwDto) {
        try {
            PublicStatus result = joinMemberService.findPw(findPwDto);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            log.error("{}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.SERVER_ERR);
        }
    }

    /**
     * 비밀번호를 변경하는 컨트롤러
     * @param changePwDto
     * @param res
     * @param req
     * @return
     */
    @PostMapping("/changePw")
    @ResponseBody
    public ResponseEntity<PublicStatus> changePasswd(@RequestBody ChangePwDto changePwDto, HttpServletResponse res, HttpServletRequest req) {
        try {
            return ResponseEntity.ok(joinMemberService.changePasswd(res, req, changePwDto));
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.SERVER_ERR);
        }
    }

    /**
     * 사용자가 Oauth 로 로그인 했을때 해당 Oauth 정보를 기존의 회원정보와 연동시키는 함수.
     * @param standardLoginDto
     * @param res
     * @param req
     * @return
     */
    @PostMapping("/linkUserByOauth")
    @ResponseBody
    public ResponseEntity<PublicStatus> linkMemberByOauth(@RequestBody StandardLoginDto standardLoginDto, HttpServletResponse res, HttpServletRequest req) {
        try {
            return ResponseEntity.ok(joinMemberService.linkMemberByOauth(standardLoginDto, res, req));
        } catch(Exception e) {
            log.error("[Error][JoinMemberController->linkMemberByOauth] {}", e.getMessage(), e);
            return ResponseEntity.ok(PublicStatus.SERVER_ERR);
        }
    }


}