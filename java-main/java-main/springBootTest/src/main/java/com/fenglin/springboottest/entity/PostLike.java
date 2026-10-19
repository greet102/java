package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 点赞表
 */
@Data
@TableName("`post_like`")
public class PostLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long postId;

    /** 点赞用户名 */
    private String username;
}
