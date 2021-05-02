package com.alan.community.service;

import com.alan.community.dao.UserMapper;
import com.alan.community.entity.LoginTicket;
import com.alan.community.entity.User;
import com.alan.community.util.CommunityConstant;
import com.alan.community.util.MailClient;
import com.alan.community.util.CommunityUtil;
import com.alan.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private  MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;

    //默认头像路径
    @Value("${defaultHeaderUrl}")
    String defaultHeaderUrl;
    @Value("${contextPath}")
    String contextPath;
    @Value("${domain}")
    String domain;

    public Map<String,Object> register(User user){
        HashMap<String, Object> map = new HashMap<>();
        //判断对象的关键信息是否为空
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        //判断是已注册
        if(userMapper.selectByName(user.getUsername()) != null){
            map.put("usernameMsg", "用户名已存在！");
            return map;
        }
        if(userMapper.selectByEmail(user.getEmail()) != null){
            map.put("emailMsg", "邮箱已注册！");
            return map;
        }

        //注册用户
        user.setCreateTime(new Date());
        user.setHeaderUrl(defaultHeaderUrl+ (int)(Math.random()*30)+".jpg");
        user.setStatus(0);
        user.setType(0);
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(CommunityUtil.generateUUID());
        userMapper.insertUser(user);

        //发送注册邮件
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        // http://localhost:8080/community
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);
        return map;
    }

    /**
     * 账号激活
     * @param userId
     * @param code
     * @return
     */
    public int activate(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //1.判空
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        //2.判断用户信息是否正确
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "用户不从在!");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        if(!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))){
         map.put("passwordMsg","密码不正确！");
         return  map;
        }

        //生成登录凭证存储在redis中
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     * @param ticket
     */
    public void logout(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return  loginTicket;
    }
}