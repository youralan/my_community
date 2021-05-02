package com.alan.community;


import com.alan.community.dao.DiscussPostMapper;
import com.alan.community.dao.elasticsearch.DiscussPostRepository;
import com.alan.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    //测试插入数据
    @Test
    public void insert(){
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void delete() {
        discussRepository.deleteById(286);
    }

    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussMapper.selectDiscussPosts(0, 0, 100, 0));
    }
    @Test
    public void testSearch() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "手机"));
        int page = 0;
        int size = 1;
        queryBuilder.withPageable(PageRequest.of(page, size));
        SearchHits<DiscussPost> hits = elasticsearchRestTemplate.search(queryBuilder.build(), DiscussPost.class);

    }
   @Test
    public void testSearchByRestTemplate() {
       NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(1, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

       SearchHits<DiscussPost> hits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);

       System.out.println(hits.getTotalHits());
       List<DiscussPost> searchResult = new ArrayList<>();
       for (SearchHit<DiscussPost> hit : hits) {
           DiscussPost post = hit.getContent();

           List<String> title = hit.getHighlightField("title");
           if(title  != null && !title.isEmpty() ){
               post.setTitle(title.get(0));
           }

           List<String> content = hit.getHighlightField("content");
           if(content  != null && !content.isEmpty() ){
               post.setTitle(content.get(0));
           }
           searchResult.add(post);
       }
       for (DiscussPost post : searchResult) {
           System.out.println(post);
       }
    }

}
