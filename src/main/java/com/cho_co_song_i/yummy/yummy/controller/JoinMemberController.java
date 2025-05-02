package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.FindIdDto;
import com.cho_co_song_i.yummy.yummy.dto.FindPwDto;
import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import com.cho_co_song_i.yummy.yummy.service.JoinMemberService;
import com.cho_co_song_i.yummy.yummy.service.KafkaProducerService;
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

    @PostMapping("/findId")
    @ResponseBody
    public ResponseEntity<PublicStatus> findId(@RequestBody FindIdDto findIdDto) {

        PublicStatus result = joinMemberService.findId(findIdDto);
        return ResponseEntity.ok(result);
    }

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

}