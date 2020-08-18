package com.tong.fpl.aop;

import com.tong.fpl.log.HttpCallLog;
import com.tong.fpl.utils.HttpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Create by tong on 2020/5/9
 */
@Aspect
@Configuration
public class HttpCallTraceAspect {

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Before(value = "@annotation(com.tong.fpl.aop.annotation.TraceHttpCall)")
    public void before(JoinPoint joinPoint) {
        MDC.put("uuid", UUID.randomUUID().toString());
        startTime.set(System.currentTimeMillis());
        Optional<ServletRequestAttributes> attributes = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        attributes.ifPresent(o -> {
            HttpServletRequest request = o.getRequest();
            MDC.put("ip", HttpUtils.getRealIp(request));
            MDC.put("url", request.getRequestURI());
            HttpCallLog.info("request:{args=%s}", Arrays.toString(joinPoint.getArgs()));
        });
    }

    @AfterReturning(returning = "object", pointcut = "@annotation(com.tong.fpl.aop.annotation.TraceHttpCall)")
    public void afterReturning(Object object) {
        HttpCallLog.info("response:{%s}", object);
        HttpCallLog.timeElapsed(System.currentTimeMillis() - startTime.get());
    }

    @AfterThrowing(throwing = "e", pointcut = "@annotation(com.tong.fpl.aop.annotation.TraceHttpCall)")
    public void afterThrowing(Exception e) {
        HttpCallLog.error("exception:{%s}", e.getMessage());
        e.printStackTrace();
    }

}
