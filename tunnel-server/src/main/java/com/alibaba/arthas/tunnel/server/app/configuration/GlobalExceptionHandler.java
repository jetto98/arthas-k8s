package com.alibaba.arthas.tunnel.server.app.configuration;

import com.alibaba.arthas.tunnel.server.app.exception.ServerException;
import com.alibaba.arthas.tunnel.server.utils.CommonResultUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResultUtil.errMsgWithData("Internal error", e.getMessage()));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity handleException(ServerException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResultUtil.errMsg(e.getMessage()));
    }
}
