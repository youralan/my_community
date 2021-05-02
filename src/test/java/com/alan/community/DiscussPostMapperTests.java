package com.alan.community;

import com.alan.community.dao.DiscussPostMapper;
import com.alan.community.entity.DiscussPost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class DiscussPostMapperTests {

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Test
    public void selectDiscussPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 5, 10,0);
        for(DiscussPost post : discussPosts){
            System.out.println(post);

        }
    }

}
