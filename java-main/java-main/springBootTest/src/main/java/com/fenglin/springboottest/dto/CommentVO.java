package com.fenglin.springboottest.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论展示对象：附带评论者的头像 / 昵称，前端直接渲染
 */
@Data
public class CommentVO {

    private Long id;

    private Long postId;

    /** 评论者用户名 */
    private String username;

    /** 评论者昵称（为空时前端回退显示用户名） */
    private String nickname;

    /** 评论者头像 */
    private String avatar;

    /** 评论内容 */
    private String content;

    /** 评论时间 */
    private LocalDateTime createdAt;
}
