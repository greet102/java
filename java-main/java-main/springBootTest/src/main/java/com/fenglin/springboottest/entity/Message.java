package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息表：sender 发给 receiver 的一条私信
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者用户名 */
    private String sender;

    /** 接收者用户名 */
    private String receiver;

    /** 消息内容 */
    private String content;

    /** 接收者是否已读（0 未读 / 1 已读） */
    private Boolean isRead;

    /** 发送时间 */
    private LocalDateTime createdAt;
}
