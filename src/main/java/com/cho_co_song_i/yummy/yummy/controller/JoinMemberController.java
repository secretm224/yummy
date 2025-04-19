package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.JoinMemberDto;
import com.cho_co_song_i.yummy.yummy.dto.PublicResponse;
import com.cho_co_song_i.yummy.yummy.service.JoinMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public ResponseEntity<PublicResponse> joinMember(@RequestBody JoinMemberDto joinMemberDto) {
        try {
            PublicResponse result = joinMemberService.joinMember(joinMemberDto);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            log.error("{}", e.getMessage(), e);
            return ResponseEntity.ok(new PublicResponse("SERVER_ERR", "API server encountered an error."));
        }
    }
}