package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.ProfileVO;
import com.fenglin.springboottest.dto.UserSummary;
import com.fenglin.springboottest.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 个人主页 / 关注 相关接口。
 * 登录态使用 HttpSession 保存（JSESSIONID Cookie），与 UserController 共用 SESSION_USER。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /** 查询某人的个人主页 */
    @GetMapping("/{username}/profile")
    public Result<ProfileVO> profile(@PathVariable String username, HttpSession session) {
        String viewer = (String) session.getAttribute(UserController.SESSION_USER);
        return Result.ok(userService.getProfile(viewer, username));
    }

    /** 查询某人的粉丝列表 */
    @GetMapping("/{username}/followers")
    public Result<List<UserSummary>> followers(@PathVariable String username, HttpSession session) {
        String viewer = (String) session.getAttribute(UserController.SESSION_USER);
        return Result.ok(userService.getFollowers(viewer, username));
    }

    /** 关注 / 取消关注 */
    @PostMapping("/{username}/follow")
    public Result<Map<String, Object>> follow(@PathVariable String username, HttpSession session) {
        String me = requireLogin(session);
        if (me.equals(username)) {
            throw new BizException("不能关注自己喵～");
        }
        return Result.ok(userService.toggleFollow(me, username));
    }

    /** 更新自己的资料 */
    @PutMapping("/me/profile")
    public Result<Void> update(@RequestBody Map<String, Object> form, HttpSession session) {
        String me = requireLogin(session);
        userService.updateProfile(me, form);
        return Result.ok();
    }

    private String requireLogin(HttpSession session) {
        String me = (String) session.getAttribute(UserController.SESSION_USER);
        if (me == null) {
            throw new BizException("未登录");
        }
        return me;
    }
}
