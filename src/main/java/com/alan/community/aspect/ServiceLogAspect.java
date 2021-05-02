package com.alan.community.aspect;

import com.alan.community.entity.User;
import com.alan.community.util.HostHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

//@Component
//@Aspect

/**
 * 面向切面编程
 * 利用Aop实现用户访问日志的统一处理不再需要到每个节点方法中去写日志处理服务
 */
public class ServiceLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLogAspect.class);
    @Autowired
    HostHolder hostHolder;

    //配置切入点
    @Pointcut("execution(* com.alan.community.service.*.*(..))")
    public void pointcut(){
    }

    //植入代码
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 用户名：[1.2.3.4],在[xxx],访问了[com.alan.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if(attributes == null){
            return;
        }
        User user =  hostHolder.getUser();
        String username = user != null ? user.getUsername() : "游客";
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        LOGGER.info(String.format("[%s]:[%s],在[%s],访问了[%s].",username, ip, now, target));
    }
}
