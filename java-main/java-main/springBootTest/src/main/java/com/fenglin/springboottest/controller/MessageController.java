package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.ConversationVO;
import com.fenglin.springboottest.dto.MessageVO;
import com.fenglin.springboottest.service.MessageService;
import com.fenglin.springboottest.controller.UserController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 私信接口：未读数 / 会话列表 / 与某人的消息线程 / 发送私信
 * 登录态与 UserController 共用 SESSION_USER（HttpSession + JSESSIONID Cookie）
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 当前用户未读私信总数 */
    @GetMapping("/unread")
    public Result<Integer> unread(HttpSession session) {
        return Result.ok(messageService.unread(requireLogin(session)));
    }

    /** 会话列表 */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations(HttpSession session) {
        return Result.ok(messageService.conversations(requireLogin(session)));
    }

    /** 与某人的全部私信（并标记已读） */
    @GetMapping("/{username}")
    public Result<List<MessageVO>> thread(@PathVariable String username, HttpSession session) {
        return Result.ok(messageService.thread(requireLogin(session), username));
    }

    /** 给某人发私信 */
    @PostMapping("/{username}")
    public Result<MessageVO> send(@PathVariable String username,
                                  @RequestBody(required = false) Map<String, String> body,
                                  HttpSession session) {
        String me = requireLogin(session);
        String content = body != null ? body.get("content") : null;
        return Result.ok(messageService.send(me, username, content));
    }

    private String requireLogin(HttpSession session) {
        String me = (String) session.getAttribute(UserController.SESSION_USER);
        if (me == null) {
            throw new BizException("未登录");
        }
        return me;
    }
}
