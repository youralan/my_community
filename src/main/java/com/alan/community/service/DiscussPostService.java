package com.alan.community.service;

import com.alan.community.dao.DiscussPostMapper;
import com.alan.community.entity.DiscussPost;
import com.alan.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 帖子业务层
 */
@Service
public class DiscussPostService {
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    DiscussPostMapper discussPostMapper;

    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }


    public int selectDiscussPostRows( int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int insertDiscussPost(DiscussPost discussPost){
        return discussPostMapper.insertDiscussPost( discussPost);
    }

    public DiscussPost selectDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }


    public int updateStatus(int id, int status){
        return  discussPostMapper.updateStatus(id, status);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }

    /**
     * 发帖请求的业务层：对发帖请求进行处理
     * @param post
     * @return
     */
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }
}
