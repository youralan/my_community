package com.alan.community.controller.interceptor;


import com.alan.community.annation.LoginRequired;
import com.alan.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    //当前用户持有者
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod method = (HandlerMethod)handler;
            //获取方法上的注解
            LoginRequired loginRequired = method.getMethodAnnotation(LoginRequired.class);
            //判断是否有这个注解，并根据当前是否持有用户判断是否能访问当前请求
            if(loginRequired != null && hostHolder.getUser() == null){
                //无权访问该请求时重定向到登录页面
                response.sendRedirect(request.getContextPath()+"/login");
                //并且不再处理后序操作
                return false;
            }
        }
        return  true;
    }
}
