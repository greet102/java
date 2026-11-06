package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知表：别人对当前用户的点赞 / 收藏 / 评论 / 关注
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收通知的用户名 */
    private String username;

    /** 触发通知的用户名 */
    private String actor;

    /** 类型：like / favorite / comment / follow */
    private String type;

    /** 相关文章ID（follow 为 NULL） */
    private Long postId;

    /** 文章标题快照 */
    private String postTitle;

    /** 评论摘要（仅 comment 类型） */
    private String content;

    /** 是否已读（0 未读 / 1 已读） */
    private Boolean isRead;

    /** 通知时间 */
    private LocalDateTime createdAt;
}
