package com.jumunhasyeotjo.order_to_shipping.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.jumunhasyeotjo.order_to_shipping.common.annotation.RequireRole;
import com.jumunhasyeotjo.order_to_shipping.common.exception.BusinessException;
import com.jumunhasyeotjo.order_to_shipping.common.exception.ErrorCode;
import com.jumunhasyeotjo.order_to_shipping.common.vo.UserRole;
import com.library.passport.proto.PassportProto;

import lombok.RequiredArgsConstructor;
import static com.library.passport.proto.PassportProto.Passport;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        Passport passport = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Passport p) {
                passport = p;
                break;
            }
        }

        if (passport == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        UserRole userRole = UserRole.valueOf(passport.getRole());
        UserRole[] allowedRoles = requireRole.value();

        boolean allowed = Arrays.stream(allowedRoles)
            .anyMatch(role -> role.equals(userRole));


        if (!allowed) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}