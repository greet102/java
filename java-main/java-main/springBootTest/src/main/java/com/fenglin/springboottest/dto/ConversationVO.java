package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 会话列表项：与某位对方的会话摘要
 */
@Data
public class ConversationVO {

    /** 对方用户名 */
    private String username;

    /** 对方昵称 */
    private String nickname;

    /** 对方头像 */
    private String avatar;

    /** 最近一条消息内容 */
    private String lastContent;

    /** 最近一条消息时间（毫秒时间戳） */
    private long lastTime;

    /** 来自对方的未读数 */
    private int unread;
}
