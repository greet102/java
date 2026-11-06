package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Notification;
import com.fenglin.springboottest.service.NotificationService;
import com.fenglin.springboottest.controller.UserController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息通知接口：未读数 / 最近通知列表 / 全部标记已读
 * 登录态与 UserController 共用 SESSION_USER（HttpSession + JSESSIONID Cookie）
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 当前用户未读通知数 */
    @GetMapping("/unread")
    public Result<Long> unread(HttpSession session) {
        return Result.ok(notificationService.unreadCount(requireLogin(session)));
    }

    /** 当前用户最近的通知列表（最多 50 条，新的在前） */
    @GetMapping
    public Result<List<Notification>> list(HttpSession session) {
        return Result.ok(notificationService.listRecent(requireLogin(session)));
    }

    /** 当前用户全部通知标记为已读 */
    @PostMapping("/read")
    public Result<Void> readAll(HttpSession session) {
        notificationService.markAllRead(requireLogin(session));
        return Result.ok(null);
    }

    private String requireLogin(HttpSession session) {
        String me = (String) session.getAttribute(UserController.SESSION_USER);
        if (me == null) {
            throw new BizException("未登录");
        }
        return me;
    }
}
