package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 登录 / 注册请求
 */
@Data
public class LoginRequest {

    private String username;
    private String password;
}
