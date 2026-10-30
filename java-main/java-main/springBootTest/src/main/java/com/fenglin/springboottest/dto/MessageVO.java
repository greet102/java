package com.fenglin.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 单条私信的返回对象，JSON 键名与前端 ChatView 对齐
 */
@Data
public class MessageVO {

    private Long id;
    private String sender;
    private String receiver;
    private String content;

    /** 接收者是否已读（强制 JSON 键名，规避 Lombok 布尔字段剥离 is 前缀的坑；用包装类型才能生成 setIsRead） */
    @JsonProperty("isRead")
    private Boolean isRead;

    /** 发送时间（毫秒时间戳，前端 timeAgo 用） */
    private long createdAt;
}
