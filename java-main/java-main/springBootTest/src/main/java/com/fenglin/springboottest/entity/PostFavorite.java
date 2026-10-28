package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 收藏表
 */
@Data
@TableName("`post_favorite`")
public class PostFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long postId;

    /** 收藏用户名 */
    private String username;
}
