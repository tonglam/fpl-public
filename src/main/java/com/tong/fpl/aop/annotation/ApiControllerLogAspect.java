package com.tong.fpl.aop.annotation;

import com.tong.fpl.log.ControllerLog;
import com.tong.fpl.utils.HttpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Create by tong on 2021/5/10
 */
@Aspect
@Component
public class ApiControllerLogAspect {

	ThreadLocal<Long> startTime = new ThreadLocal<>();
	StringBuffer stringBuffer;

	@Pointcut("execution(public * com.tong.fpl.controller.api.*.*(..))")
	public void controllerLog() {
	}

	@Before("controllerLog()")
	public void doBefore(JoinPoint joinPoint) {
		startTime.set(System.currentTimeMillis());
		stringBuffer = new StringBuffer();
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			// ip
			HttpServletRequest request = attributes.getRequest();
			MDC.put("ip", HttpUtils.getRealIp(request));
			// params
			stringBuffer.append("url:{").append(request.getRequestURI()).append("}");
			stringBuffer.append(", method:{").append(request.getMethod()).append("}");
			stringBuffer.append(", args:{").append(Arrays.toString(joinPoint.getArgs())).append("}");
		}
	}

	@After("controllerLog()")
	public void doAfter() {
	}

	@AfterReturning(returning = "obj", pointcut = "controllerLog()")
	public void doAfterReturning(Object obj) {
		stringBuffer.append(", response:{data}");
		stringBuffer.append(", elapsed time:").append(System.currentTimeMillis() - startTime.get()).append("ms!");
		ControllerLog.info(stringBuffer.toString());
	}

}
