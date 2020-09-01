package com.tong.fpl.config.web;

import com.tong.fpl.domain.letletme.global.ResponseData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.utils.JsonUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Create by tong on 2020/8/18
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	@Nullable
	public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		if (body == null) {
			return ResponseData.success();
		}
		// 拦截table数据返回前段
		if (body instanceof TableData<?>) {
			return body;
		}
		// 拦截api数据
		if (returnType.getExecutable().getClass().getSimpleName().contains("Http")) {
			if (body instanceof ResponseData) {
				return body;
			}
			if (body instanceof String) {
				return JsonUtils.obj2json(ResponseData.success(body.toString()));
			}
		}
		return body;
	}

}
