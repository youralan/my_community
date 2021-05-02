package com.alan.community.dao;

import com.alan.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查询帖子信息
 */
@Mapper
public interface  DiscussPostMapper {
    /**
     * 从数据库查询多个帖子
     * @param userId 是为个人主页中查询个人帖子时使用的，当userID = 0时忽略它
     * @param offset 是每一页起始行的行号
     * @param limit 每一页最多显示的帖子数
     * @param orderMode 排序模式，普通排序0 按照score排序1
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    // @Param注解用于给参数取别名,在此处别名也参数名一致
    // 如果方法只有一个参数,并且参数在<if>里使用,则必须给参数加别名不然会报错.
    //userId = 0时同样忽略，是为个人主页查询时使用的
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateStatus(int id, int status);

    int updateType(int id, int type);

    int updateScore(int id, double score);
}
