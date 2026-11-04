package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 发表评论的表单
 */
@Data
public class CommentForm {

    /** 评论内容 */
    private String content;
}
