package com.tong.fpl.aop;

import com.tong.fpl.log.ControllerLog;
import com.tong.fpl.utils.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * Create by tong on 2020/5/9
 */
@Aspect
@Component
public class ControllerLogAspect {

    ThreadLocal<Long> startTime = new ThreadLocal<>();
    StringBuffer stringBuffer;

    @Pointcut("execution(public * com.tong.fpl.controller.*.*(..))")
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
            // entry
            HttpSession session = request.getSession();
            String entry = "未选择";
            if (session.getAttribute("entry") != null) {
                entry = String.valueOf(session.getAttribute("entry"));
            }
            MDC.put("entry", entry);
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
