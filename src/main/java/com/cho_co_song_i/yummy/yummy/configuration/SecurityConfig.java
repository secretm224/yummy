package com.cho_co_song_i.yummy.yummy.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.cors.access.urls}")
    private List<String> accessOriginUrls;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) /* CORS 설정 추가 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()  /* 모든 요청 허용 */
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        /* 허용할 Origin URL 설정 */
        configuration.setAllowedOrigins(accessOriginUrls);

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
