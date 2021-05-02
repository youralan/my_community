package com.alan.community.controller;

import com.alan.community.entity.DiscussPost;
import com.alan.community.entity.Page;
import com.alan.community.service.LikeService;
import com.alan.community.service.SearchService;
import com.alan.community.service.UserService;
import com.alan.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子
        Map<String, Object> searchResult = searchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.get("total"));

        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        List<DiscussPost> posts = (List<DiscussPost>) searchResult.get("posts");
        if ( posts!= null && !posts.isEmpty()) {
            for (DiscussPost post : posts) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);


        return "/site/search";
    }
}
