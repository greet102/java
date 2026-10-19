package com.fenglin.springboottest.dto;

import lombok.Data;

import java.util.List;

/**
 * 返回给前端的文章对象，字段与前端 store.js 中完全一致
 */
@Data
public class PostVO {

    private Long id;
    private String author;
    private String emoji;
    private String title;
    private String body;
    private Long ts;

    /** 点赞用户名列表 */
    private List<String> likes;
}
