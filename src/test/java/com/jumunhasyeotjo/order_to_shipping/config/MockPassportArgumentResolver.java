package com.jumunhasyeotjo.order_to_shipping.config;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.library.passport.proto.PassportProto.Passport;

// Controller Passport Mock 주입 클래스
public class MockPassportArgumentResolver implements HandlerMethodArgumentResolver {

    private final Passport passport;

    public MockPassportArgumentResolver(Passport passport) {
        this.passport = passport;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Passport.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return this.passport;
    }
}