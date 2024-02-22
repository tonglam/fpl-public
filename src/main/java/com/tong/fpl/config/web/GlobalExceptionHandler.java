package com.tong.fpl.config.web;

import com.tong.fpl.domain.letletme.global.ResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Create by tong on 2020/8/18
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public <T> ResponseData<T> exceptionHandler(Exception e) {
		e.printStackTrace();
		return ResponseData.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器异常：" + e.getMessage());
	}

}
