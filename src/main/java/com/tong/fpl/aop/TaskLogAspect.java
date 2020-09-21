package com.tong.fpl.aop;

import com.tong.fpl.log.TaskLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create by tong on 2020/9/21
 */
@Aspect
@Component
public class TaskLogAspect {

	@Pointcut("execution(public * com.tong.fpl.task.*.*(..))")
	public void taskLog() {
	}

	@Around(value = "taskLog()")
	public Object around(ProceedingJoinPoint point) {
		long startTime = System.currentTimeMillis();
		Object object = null;
		try {
			object = point.proceed();
		} catch (Throwable throwable) {
			TaskLog.error(throwable.getMessage());
			throwable.printStackTrace();
		}
		long timeTaken = System.currentTimeMillis() - startTime;
		TaskLog.info("run task:" + point.getSignature() + ", run time:" + LocalDateTime.now() + ", escaped:" + timeTaken);
		return object;
	}

}
