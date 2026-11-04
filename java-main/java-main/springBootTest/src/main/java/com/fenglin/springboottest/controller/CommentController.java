package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.CommentForm;
import com.fenglin.springboottest.dto.CommentVO;
import com.fenglin.springboottest.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论接口：查看 / 发表 / 删除
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 某篇文章的评论列表（无需登录） */
    @GetMapping("/posts/{postId}/comments")
    public Result<List<CommentVO>> list(@PathVariable Long postId) {
        return Result.ok(commentService.listByPost(postId));
    }

    /** 发表评论（需登录） */
    @PostMapping("/posts/{postId}/comments")
    public Result<CommentVO> add(@PathVariable Long postId,
                                 @RequestBody CommentForm form,
                                 HttpSession session) {
        String username = requireLogin(session);
        return Result.ok("评论成功", commentService.add(username, postId, form == null ? null : form.getContent()));
    }

    /** 删除评论：评论作者本人，或已登录后台的管理员 */
    @DeleteMapping("/comments/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        String username = requireLogin(session);
        boolean isAdmin = session.getAttribute(AdminController.SESSION_ADMIN) != null;
        commentService.delete(username, id, isAdmin);
        return Result.ok("已删除", null);
    }

    private String requireLogin(HttpSession session) {
        String username = (String) session.getAttribute(UserController.SESSION_USER);
        if (username == null) {
            throw new BizException("请先登录喵～");
        }
        return username;
    }
}
