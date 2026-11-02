package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 后台数据概览
 */
@Data
public class AdminStatsVO {

    /** 注册用户总数 */
    private long userCount;

    /** 文章总数 */
    private long postCount;

    /** 关注关系总数 */
    private long followCount;

    /** 今日新增用户 */
    private long todayUserCount;
}
