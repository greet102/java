package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 新增 / 编辑文章请求
 */
@Data
public class PostForm {

    private String title;
    private String body;
    private String emoji;
}
