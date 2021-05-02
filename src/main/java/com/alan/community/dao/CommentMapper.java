package com.alan.community.dao;

import com.alan.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //通过对象的类型和id查询对象对应的帖子
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    //统计对应对象的评论数量
    int selectCountByEntity(int entityType, int entityId);

    //添加评论
    int insertComment(Comment comment);

    //通过评论id直接查找评论
    Comment selectCommentById(int id);
}
