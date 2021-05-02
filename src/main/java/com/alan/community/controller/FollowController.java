package com.alan.community.controller;

import com.alan.community.entity.Event;
import com.alan.community.entity.Page;
import com.alan.community.entity.User;
import com.alan.community.event.EventProducer;
import com.alan.community.service.FollowService;
import com.alan.community.service.UserService;
import com.alan.community.util.CommunityConstant;
import com.alan.community.util.CommunityUtil;
import com.alan.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0,"已取消关注！");
    }


    //获取某个用户的关注列表
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    /**
     * 查询某个用户的粉丝列表
     */
        @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
        public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
            User user = userService.findUserById(userId);
            if (user == null) {
                throw new RuntimeException("该用户不存在!");
            }
            model.addAttribute("user", user);

            page.setLimit(5);
            page.setPath("/followers/" + userId);
            page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

            List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
            if (userList != null) {
                for (Map<String, Object> map : userList) {
                    User u = (User) map.get("user");
                    map.put("hasFollowed", hasFollowed(u.getId()));
                }
            }
            model.addAttribute("users", userList);

            return "/site/follower";
        }

    /**
     * 判断当前用户是否已关注某个用户
     * @param userId
     * @return
     */
    public boolean hasFollowed(int userId){
        User user = hostHolder.getUser();
        if(user == null) {
            return false;
        }

        return followService.hasFollowed(user.getId(), ENTITY_TYPE_USER, userId);
    }
}
