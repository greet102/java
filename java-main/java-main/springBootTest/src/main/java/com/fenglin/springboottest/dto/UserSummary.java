package com.fenglin.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用户简要信息，用于“我的关注”等列表（字段与前端 UserRow 完全一致）
 */
@Data
public class UserSummary {

    private String username;
    private String nickname;
    private String slogan;
    private String avatar;

    /** 当前登录用户是否已关注 TA（前端 UserRow 的“已关注”状态用） */
    @JsonProperty("isFollowing")
    private boolean isFollowing;
}
