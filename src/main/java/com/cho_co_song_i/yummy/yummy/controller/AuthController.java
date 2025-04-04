package com.cho_co_song_i.yummy.yummy.controller;
import com.cho_co_song_i.yummy.yummy.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "JWT 토큰 발급용 임시 API")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/token")
    @Operation(summary = "임시 JWT 토큰 발급", description = "유저 이름을 넣으면 테스트용 JWT 토큰을 발급합니다.")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam(name = "username", defaultValue = "트럼프") String username) {
        String token = jwtTokenProvider.createToken(username, "USER");
        return ResponseEntity.ok(Map.of("token", token));
    }
}