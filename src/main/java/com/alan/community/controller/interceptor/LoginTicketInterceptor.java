package com.alan.community.controller.interceptor;

import com.alan.community.entity.LoginTicket;
import com.alan.community.entity.User;
import com.alan.community.service.UserService;
import com.alan.community.util.CookieUtil;
import com.alan.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 用户登录拦截器，通过http头中的cookie来实现自动登录，在本次请求中记住用户
 */

//自定义实现Spring的HandlerInterceptor接口的拦截器
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 在业务处理器处理请求之前被调用。
     * 预处理，可以进行编码、安全控制、权限校验等处理；
     * 此处的作用是根据请求中的ticket cookie 确认当前的请求用户
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头中获取登录凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if(ticket != null){
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //判断当前登录凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                //持有当前登录的用户
                hostHolder.setUserThreadLocal(user);
            }
        }
        //继续执行之后的操作
        return true;
    }

    @Override
    //业务处理器处理请求执行完成后，生成视图之前执行。用于修改modelAndView
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    //在DispatcherServlet完全处理完请求后被调用，可用于清理资源等.
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //不清除资源会造成垃圾的积累
        hostHolder.clear();
    }
}
