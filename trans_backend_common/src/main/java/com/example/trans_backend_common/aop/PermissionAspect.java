package com.example.trans_backend_common.aop;


import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.constant.UserConstant;
import com.example.trans_backend_common.enetity.User;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
@Aspect
public class PermissionAspect {

    @Pointcut("@annotation(AuthCheck)")
    public void pointCut(){

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        AuthCheck annotation = method.getAnnotation(AuthCheck.class);
        if(annotation==null|| !annotation.required()){
            return proceedingJoinPoint.proceed();
        }
        //判断权限
        String[] permission = annotation.permission();
        //如果没有权限要求，直接放行
        if(permission.length==0){
            return proceedingJoinPoint.proceed();
        }
        ServletRequestAttributes requestAttributes =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader(UserConstant.TOKEN);
        ThrowUtils.throwIf(token==null, ErrorCode.NOT_LOGIN_ERROR);
        //解析出user
        User user = JSONUtil.toBean(token, User.class);
        String userRole = user.getUserRole();
        for (String s : permission) {
            if (s.equals(userRole)) {
                return proceedingJoinPoint.proceed();
            }
        }
        //没有权限
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }


}
