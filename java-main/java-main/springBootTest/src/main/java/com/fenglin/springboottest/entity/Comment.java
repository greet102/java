package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表：用户在文章下发表的评论
 * 注意：comment 是 MySQL 8 保留字，表名必须加反引号
 */
@Data
@TableName("`comment`")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long postId;

    /** 评论者用户名 */
    private String username;

    /** 评论内容 */
    private String content;

    /** 评论时间 */
    private LocalDateTime createdAt;
}
