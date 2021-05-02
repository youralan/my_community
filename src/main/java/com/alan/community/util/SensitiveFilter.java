package com.alan.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤工具类
 * 思路：敏感词存储在本地的一个文本文件里，启动时初始化一个敏感词前缀树；用户发文本信息时可以调用filter方法，获得过滤后的字符串
 */
@Component
public class SensitiveFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);
    //敏感词替换符
    private static final  String REPLACEMENT = "***";
    //前缀树根节点
    private TireNode root = new TireNode();

    //1.自定义前缀树
    class TireNode{
        //关键词末尾标识
        private boolean isEnd = false;
        //子节点
        private Map<Character, TireNode> subNodes = new HashMap<>();

        public boolean getIsEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }

        public  TireNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        public TireNode setSubNode(Character c) {
            TireNode subNode = new TireNode();
            subNodes.put(c, subNode);
            return subNode;
        }
    }

    //2.往前缀树里面添加单词的方法
    public void addWord(String word){
        if(StringUtils.isBlank(word)) return;
        char[] chars = word.toCharArray();

        TireNode node = root;
        for (char c : chars) {
            TireNode subNode = node.getSubNode(c);
            if(subNode == null){
                TireNode subNode1 = node.setSubNode(c);
                node = subNode1;
            }else{
                node = subNode;
            }
        }
        node.setEnd(true);
    }

    //3.特殊字符判断，防破解
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //4.前缀树初始化方法
    //该注解是JavaEE中的注解，用来完成一个对象创建时的初始化，在init方法之后执行
    @PostConstruct
    private void init(){

        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addWord(keyword);
            }
        } catch (IOException e) {
            LOGGER.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    //5.字符串过滤的方法
    /**
     * 字符串过滤的方法
     * @param s 需要过滤的字符串
     * @return 过滤好的字符串
     */
    public String filter(String s){
        StringBuilder f = new StringBuilder();
        char[] chars = s.toCharArray();
        //指针1
        TireNode temp = root;
        //指针2
        int begin = 0;
        //指针3
        int curr = 0;
        for(int  i = 0; i < s.length(); i++){
            if(isSymbol(chars[curr]) ){
                if(temp == root){
                    f.append(chars[curr]);
                    begin++;
                }
                curr++;
                continue;
            }

            //判断下一个节点
            temp = temp.getSubNode(chars[curr]);
            if(temp == null){
                f.append(chars[curr]);
                curr = ++begin;
                temp = root;
            }else if(temp.getIsEnd()){
                f.append(REPLACEMENT);
                begin = ++curr;
                temp = root;
            }else {
                curr++;
            }
        }

        //添加末尾可能遗留的字符串
        f.append(s.substring(begin));
        return f.toString();
    }
}
