package com.jumunhasyeotjo.order_to_shipping.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class MetricAspect {

    @Around("@annotation(com.jumunhasyeotjo.order_to_shipping.common.aspect.Metric) || " +
            "execution(* com.jumunhasyeotjo.order_to_shipping..infrastructure.external..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Metric annotation = AnnotatedElementUtils.findMergedAnnotation(method, Metric.class);

        if (annotation == null) {
            return joinPoint.proceed();
        }

        String taskName = annotation.value();
        if (!StringUtils.hasText(taskName)) {
            taskName = method.getName();
        }

        // 설정된 로그 레벨 가져오기
        Metric.Level level = annotation.level();

        long startTime = System.currentTimeMillis();

        try {
            // [Start] 로그: 설정된 레벨로 출력
            logWithLevel(level, "[Start] {} 시작", taskName);

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;

            // [Success] 로그: 설정된 레벨로 출력
            logWithLevel(level, "[Success] {} 완료 ({}ms)", taskName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            // [Fail] 로그: 실패는 심각한 문제이므로 설정과 상관없이 ERROR로 고정
            log.error("[Fail] {} 실패 ({}ms) - Reason: {}", taskName, duration, e.getMessage());
            throw e;
        }
    }

    // 로그 레벨에 따른 분기 처리
    private void logWithLevel(Metric.Level level, String format, Object... arguments) {
        switch (level) {
            case TRACE -> log.trace(format, arguments);
            case DEBUG -> log.debug(format, arguments);
            case WARN  -> log.warn(format, arguments);
            case ERROR -> log.error(format, arguments);
            default    -> log.info(format, arguments);
        }
    }
}