package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章表
 */
@Data
@TableName("`post`")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者用户名 */
    private String author;

    /** 心情表情 */
    private String emoji;

    /** 标题 */
    private String title;

    /** 正文 */
    private String body;

    /** 发布时间（毫秒时间戳，与前端一致） */
    private Long ts;
}
