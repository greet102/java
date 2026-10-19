package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册 / 登录 / 退出 / 当前用户
 * 登录态使用 HttpSession 保存（JSESSIONID Cookie）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    public static final String SESSION_USER = "loginUser";

    private final UserService userService;

    /** 注册，成功后自动登录 */
    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginRequest req, HttpSession session) {
        User user = userService.register(req.getUsername(), req.getPassword());
        session.setAttribute(SESSION_USER, user.getUsername());
        return Result.ok("注册成功", user.getUsername());
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest req, HttpSession session) {
        User user = userService.login(req.getUsername(), req.getPassword());
        session.setAttribute(SESSION_USER, user.getUsername());
        return Result.ok("登录成功", user.getUsername());
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok("已退出", null);
    }

    /** 查询当前登录用户（用于页面刷新后恢复会话） */
    @GetMapping("/me")
    public Result<String> me(HttpSession session) {
        String username = (String) session.getAttribute(SESSION_USER);
        if (username == null) {
            throw new BizException("未登录");
        }
        return Result.ok(username);
    }
}
