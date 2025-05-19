package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.*;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.JoinMamberService;
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

    private final JoinMamberService joinMamberService;

    public JoinMemberController(JoinMamberService joinMamberService) {
        this.joinMamberService = joinMamberService;
    }

    /**
     * 회원가입 해주는 컨트롤러 함수
     * @param joinMemberDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/join")
    @ResponseBody
    public ResponseEntity<PublicStatus> joinMember(
            @RequestBody JoinMemberDto joinMemberDto,
            HttpServletResponse res,
            HttpServletRequest req
    ) throws Exception {
        return ResponseEntity.ok(joinMamberService.joinMember(res, req, joinMemberDto));
    }

    /**
     * 아이디를 찾아주는 컨트롤러
     * @param findIdDto
     * @return
     * @throws Exception
     */
    @PostMapping("/findId")
    @ResponseBody
    public ResponseEntity<PublicStatus> recoverUserId(@RequestBody FindIdDto findIdDto) throws Exception {
        return ResponseEntity.ok(joinMamberService.recoverUserId(findIdDto));
    }

    /**
     * 비밀번호를 찾아주는 컨트롤러
     * @param findPwDto
     * @return
     * @throws Exception
     */
    @PostMapping("/findPw")
    @ResponseBody
    public ResponseEntity<PublicStatus> recoverUserPw(@RequestBody FindPwDto findPwDto) throws Exception {
        return ResponseEntity.ok(joinMamberService.recoverUserPw(findPwDto));
    }

    /**
     * 비밀번호를 변경하는 컨트롤러
     * @param changePwDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/changePw")
    @ResponseBody
    public ResponseEntity<PublicStatus> changePasswd(
            @RequestBody ChangePwDto changePwDto,
            HttpServletResponse res,
            HttpServletRequest req
    ) throws Exception {
        return ResponseEntity.ok(joinMamberService.changePasswd(res, req, changePwDto));
    }

    /**
     * 사용자가 Oauth 로 로그인 했을때 해당 Oauth 정보를 기존의 회원정보와 연동시키는 함수.
     * @param standardLoginDto
     * @param res
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/linkUserByOauth")
    @ResponseBody
    public ResponseEntity<PublicStatus> linkMemberByOauth(
            @RequestBody StandardLoginDto standardLoginDto,
            HttpServletResponse res,
            HttpServletRequest req
    ) throws Exception {
        return ResponseEntity.ok(joinMamberService.linkMemberByOauth(standardLoginDto, res, req));
    }

    /**
     * 회원가입 인증코드 발송
     * @param userEmail
     * @return
     * @throws Exception
     */
    @PostMapping("/verificationEmailCode")
    @ResponseBody
    public PublicStatus getVerificationCode(@RequestParam("userEmail") String userEmail) throws Exception {
        return joinMamberService.generateVerificationCode(userEmail);
    }

    /**
     * 회원가입인증코드검증
     * @param userEmail
     * @param code
     * @return
     * @throws Exception
     */
    @PostMapping("/checkVerificationCode")
    @ResponseBody
    public PublicStatus checkVerificationCode(@RequestParam("userEmail") String userEmail,
                                              @RequestParam("code") int code) throws Exception{
        return joinMamberService.checkVerificationCode(userEmail,code);
    }
}