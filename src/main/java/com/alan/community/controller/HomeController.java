package com.alan.community.controller;

import com.alan.community.entity.DiscussPost;
import com.alan.community.entity.Page;
import com.alan.community.entity.User;
import com.alan.community.service.DiscussPostService;
import com.alan.community.service.LikeService;
import com.alan.community.service.MessageService;
import com.alan.community.service.UserService;
import com.alan.community.util.CommunityConstant;
import com.alan.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理主页请求
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
   private DiscussPostService discussPostService;

    @Autowired
   private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;


    /**
     * 实现分页显示
     * @return 返回主页面
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getHomePage(Page page, Model model,
                              @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.

        //处理页面所需数据
        int rows = discussPostService.selectDiscussPostRows(0);
        page.setRows(rows);
        page.setPath("/index?orderMode=" + orderMode);
        //查询帖子
        List<DiscussPost> discussPosts = discussPostService.
                selectDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        //封装帖子与用户的链表
        List<Map<String, Object>> posts = new ArrayList<>();
        //将帖子数据与其对应的对象数据之间对应起来
        if(discussPosts != null){
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);

                posts.add(map);
            }
        }
        //帖子排序模式
        model.addAttribute("orderMode",orderMode);
        //帖子数据
        model.addAttribute("discussPosts",posts);

        return "index";
    }
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
