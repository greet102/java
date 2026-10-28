package com.fenglin.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 个人主页返回对象，字段与前端 store.js 中 state.profile / myProfile 完全一致
 */
@Data
public class ProfileVO {

    private String username;
    private String nickname;
    private String slogan;
    private String bio;
    private String avatar;
    private String location;
    private LocalDate birthday;

    /** 粉丝数 */
    private long followerCount;

    /** 关注数 */
    private long followingCount;

    /** 当前登录用户是否已关注 TA */
    @JsonProperty("isFollowing")
    private boolean isFollowing;

    /** TA 关注的人（看自己时用于“我的关注”） */
    private List<UserSummary> followings;
}
