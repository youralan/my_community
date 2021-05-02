package com.alan.community.util;

import com.alan.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 利用ThreadLocal来存储当前线程对应的user
 */
@Component
public class HostHolder {

    private ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public void setUserThreadLocal(User user){
        userThreadLocal.set(user);
    }

    public User getUser(){
        return userThreadLocal.get();
    }

    public void clear(){
        userThreadLocal.remove();
    }

}
