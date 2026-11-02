package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminStatsVO;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台管理接口：管理员登录 + 对前台「用户」数据进行管理（查询 / 编辑 / 删除）。
 * 登录态使用 HttpSession 中独立的 adminUser 属性，与前端用户的登录态互不干扰。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    /** 会话中保存管理员账号的属性名（与前端用户 loginUser 区分） */
    public static final String SESSION_ADMIN = "adminUser";

    private final UserService userService;

    @Value("${blog.admin.username:admin}")
    private String adminUsername;

    @Value("${blog.admin.password:admin123}")
    private String adminPassword;

    /** 管理员登录（仅登录，无注册） */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest req, HttpSession session) {
        String u = trim(req == null ? null : req.getUsername());
        String p = req == null ? null : req.getPassword();
        if (!adminUsername.equals(u) || !adminPassword.equals(p)) {
            throw new BizException("管理员账号或密码错误喵～");
        }
        session.setAttribute(SESSION_ADMIN, adminUsername);
        return Result.ok("登录成功", adminUsername);
    }

    /** 退出后台 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.removeAttribute(SESSION_ADMIN);
        return Result.ok("已退出后台", null);
    }

    /** 查询当前是否已登录后台（用于刷新后恢复会话） */
    @GetMapping("/me")
    public Result<String> me(HttpSession session) {
        String admin = (String) session.getAttribute(SESSION_ADMIN);
        if (admin == null) {
            throw new BizException("未登录");
        }
        return Result.ok(admin);
    }

    /** 数据概览 */
    @GetMapping("/stats")
    public Result<AdminStatsVO> stats(HttpSession session) {
        requireAdmin(session);
        return Result.ok(userService.adminStats());
    }

    /** 用户分页列表（支持按用户名 / 昵称搜索） */
    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            HttpSession session) {
        requireAdmin(session);
        return Result.ok(userService.adminListUsers(keyword, page, size));
    }

    /** 单个用户详情 */
    @GetMapping("/users/{username}")
    public Result<User> getUser(@PathVariable String username, HttpSession session) {
        requireAdmin(session);
        User u = userService.getUser(username);
        if (u == null) {
            throw new BizException("用户不存在喵～");
        }
        return Result.ok(u);
    }

    /** 编辑用户资料 / 重置密码 */
    @PutMapping("/users/{username}")
    public Result<Void> updateUser(
            @PathVariable String username,
            @RequestBody Map<String, Object> form,
            HttpSession session) {
        requireAdmin(session);
        userService.adminUpdateUser(username, form);
        return Result.ok("已保存", null);
    }

    /** 删除用户（级联删除其文章、点赞、收藏、关注、私信） */
    @DeleteMapping("/users/{username}")
    public Result<Void> deleteUser(@PathVariable String username, HttpSession session) {
        requireAdmin(session);
        userService.adminDeleteUser(username);
        return Result.ok("已删除", null);
    }

    /** 校验是否已登录后台，未登录直接抛异常 */
    private void requireAdmin(HttpSession session) {
        if (session.getAttribute(SESSION_ADMIN) == null) {
            throw new BizException("未登录");
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
