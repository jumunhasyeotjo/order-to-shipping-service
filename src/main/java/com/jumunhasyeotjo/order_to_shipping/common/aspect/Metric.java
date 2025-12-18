package com.jumunhasyeotjo.order_to_shipping.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Metric {
    String value(); // 작업 명
    Level level() default Level.INFO; // 기본값은 INFO

    enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}