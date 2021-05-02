package com.alan.community.controller;

import com.alan.community.annation.LoginRequired;
import com.alan.community.entity.User;
import com.alan.community.service.FollowService;
import com.alan.community.service.LikeService;
import com.alan.community.service.UserService;
import com.alan.community.util.CommunityConstant;
import com.alan.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     * 获取个人设置页面
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model){
        return "/site/setting";
    }

    /**
     * 获取个人信息页面
     */
    // 请求获取用户个人主页,请求中带有被查看的用户的id
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        //根据用户id查询用户
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        //将用户信息传给前台
        model.addAttribute("user", user);
        // 获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 获关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前用户是否已关注该用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            //在redis中查询当前用户是否已经关注该用户
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        //信息出传递给前台
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
