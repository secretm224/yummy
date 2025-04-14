package com.cho_co_song_i.yummy.yummy.configuration;

import com.cho_co_song_i.yummy.yummy.jwt.JwtAuthenticationFilter;
import com.cho_co_song_i.yummy.yummy.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Value("${spring.security.cors.access.urls}")
    private List<String> accessOriginUrls;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) /* 해당 설정이 없으면 Post 요청을 기본적으로 차단함 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) /* CORS 설정 추가 */
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/auth/token", // 토큰 발급 API는 허용
                        "/error",      // 오류 페이지 허용
                        "/favicon.ico" // 기타 정적 자원
                    ).permitAll()
                    .anyRequest().permitAll() // 모든 요청은 허용 → 실제 인증은 필터에서만 Swagger에 대해 수행
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
                 //  .requestMatchers("/**").permitAll()  /* 모든 요청 허용 */
                //);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        /* 허용할 Origin URL 설정 */
        //configuration.setAllowedOriginPatterns(accessOriginUrls);
        configuration.setAllowedOriginPatterns(List.of("http://www.seunghwan-dev.kro.kr:*"));

        /* 허용할 HTTP 메서드 */
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        /* 모든 요청 헤더 허용 */
        configuration.setAllowedHeaders(List.of("*"));

        /* 인증 정보 포함 허용 */
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;  // UrlBasedCorsConfigurationSource 타입 그대로 반환
    }
}
