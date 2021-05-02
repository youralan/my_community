package com.alan.community.controller;

import com.alan.community.annation.LoginRequired;
import com.alan.community.entity.Comment;
import com.alan.community.entity.DiscussPost;
import com.alan.community.entity.Event;
import com.alan.community.event.EventProducer;
import com.alan.community.service.CommentService;
import com.alan.community.service.DiscussPostService;
import com.alan.community.util.CommunityConstant;
import com.alan.community.util.HostHolder;
import com.alan.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        if (!StringUtils.isBlank(comment.getContent())) {
            comment.setUserId(hostHolder.getUser().getId());
            comment.setCreateTime(new Date());
            comment.setStatus(0);
            commentService.addComment(comment);

            // 触发评论事件
            Event event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(comment.getEntityType())
                    .setEntityId(comment.getEntityId())
                    .setData("postId", discussPostId);
            if (comment.getEntityType() == ENTITY_TYPE_POST) {
                DiscussPost target = discussPostService.selectDiscussPostById(comment.getEntityId());
                event.setEntityUserId(target.getUserId());
            } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
                Comment target = commentService.findCommentById(comment.getEntityId());
                event.setEntityUserId(target.getUserId());
            }
            eventProducer.fireEvent(event);

            if (comment.getEntityType() == ENTITY_TYPE_POST) {
                // 触发发帖事件
                event = new Event()
                        .setTopic(TOPIC_PUBLISH)
                        .setUserId(comment.getUserId())
                        .setEntityType(ENTITY_TYPE_POST)
                        .setEntityId(discussPostId);
                eventProducer.fireEvent(event);
                // 计算帖子分数
                String redisKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(redisKey, discussPostId);
            }
        }
            return "redirect:/discuss/detail/" + discussPostId;
    }
}