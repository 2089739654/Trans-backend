package com.example.trans_backend_common.aop;


import com.example.trans_backend_common.aop.ResourceCheck;
import com.example.trans_backend_common.aop.ResourceCheckService;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.enums.ResourceTypeEnum;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@ConditionalOnBean(value = {ResourceCheckService.class})
public class ResourceCheckAspect {

    @Resource
    private ResourceCheckService resourceCheckService;


    @Pointcut("@annotation(com.example.trans_backend_common.aop.ResourceCheck)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ThrowUtils.throwIf(resourceCheckService==null, ErrorCode.SYSTEM_ERROR, "ResourceCheckService未注入");
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        ResourceCheck annotation = method.getAnnotation(ResourceCheck.class);
        String checkResource = annotation.checkResource();
        ResourceTypeEnum resourceTypeEnum = annotation.resourceType();

        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 向上下文中注册方法参数（以参数名作为变量名）
        String[] paramNames = signature.getParameterNames();
        Object[] args = proceedingJoinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]); // 变量名 = 参数名，值 = 参数值
        }

        // 解析并计算表达式
        Expression expression = parser.parseExpression(checkResource);
        Object value = expression.getValue(context);
        if (!(value instanceof Long)) {
            log.error("参数类型错误" + value);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数类型错误");
        }
        Long resourceId = (Long) value;
        User user = BaseContext.getUser();
        if (user == null) {
            log.error("用户未登录");
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        if(!resourceCheckService.doCheck(user.getId(),resourceTypeEnum,resourceId)){
            log.error("用户没有权限访问资源" + resourceTypeEnum);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户没有权限访问资源");
        }
        return proceedingJoinPoint.proceed();

    }


}
