package com.alan.community.service;

import com.alan.community.dao.elasticsearch.DiscussPostRepository;
import com.alan.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }

    public Map<String, Object> searchDiscussPost(String keyword, int current, int limit) {
        Map<String, Object> searchResult = new HashMap<>();//封装结果
        //1.查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //2.查询结果处理
        SearchHits<DiscussPost> hits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        searchResult.put("total",(int)hits.getTotalHits());//结果总数用于分页
        List<DiscussPost> posts = new ArrayList<>();
        for (SearchHit<DiscussPost> hit : hits) {
            DiscussPost post = hit.getContent();

            List<String> title = hit.getHighlightField("title");
            if(title  != null && !title.isEmpty() ){
                post.setTitle(title.get(0));
            }

            List<String> content = hit.getHighlightField("content");
            if(content  != null && !content.isEmpty() ){
                post.setContent(content.get(0));
            }
            posts.add(post);
        }
        searchResult.put("posts",posts);//结果
        return searchResult;
    }
}
