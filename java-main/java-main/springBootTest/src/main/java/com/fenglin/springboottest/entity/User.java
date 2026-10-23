package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（演示项目，明文存储） */
    private String password;

    /** 头像：用一个表情充当头像 */
    private String avatar;

    /** 昵称，为空时前端回退显示用户名 */
    private String nickname;

    /** 个人主页标语（一句话签名） */
    private String slogan;

    /** 个人简介 */
    private String bio;

    /** 所在地 */
    private String location;

    /** 生日 */
    private LocalDate birthday;

    /** 注册时间 */
    private LocalDateTime createdAt;
}
