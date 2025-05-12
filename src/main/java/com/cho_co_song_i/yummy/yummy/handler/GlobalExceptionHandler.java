package com.cho_co_song_i.yummy.yummy.handler;

import com.cho_co_song_i.yummy.yummy.enums.PublicStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice  /* 모든 @RestController 또는 @Controller 대상으로 작동 */
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public PublicStatus handleAllExceptions(Exception e, HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        log.error("[GlobalException Error][{} {}] {}", method, path, e.getMessage(), e);
        return PublicStatus.SERVER_ERR;
    }
}