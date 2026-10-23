package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.PostForm;
import com.fenglin.springboottest.dto.PostVO;
import com.fenglin.springboottest.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章接口：列表 / 新增 / 编辑 / 删除 / 点赞
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 文章列表 */
    @GetMapping
    public Result<List<PostVO>> list() {
        return Result.ok(postService.listPosts());
    }

    /** 新增文章 */
    @PostMapping
    public Result<PostVO> add(@RequestBody PostForm form, HttpSession session) {
        String username = requireLogin(session);
        return Result.ok("发布成功", postService.addPost(username, form));
    }

    /** 编辑文章 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody PostForm form,
                               HttpSession session) {
        String username = requireLogin(session);
        postService.updatePost(username, id, form);
        return Result.ok("已更新", null);
    }

    /** 删除文章 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        String username = requireLogin(session);
        postService.deletePost(username, id);
        return Result.ok("已删除", null);
    }

    /** 点赞 / 取消点赞 */
    @PostMapping("/{id}/like")
    public Result<List<String>> like(@PathVariable Long id, HttpSession session) {
        String username = requireLogin(session);
        return Result.ok(postService.toggleLike(username, id));
    }

    /** 收藏 / 取消收藏 */
    @PostMapping("/{id}/favorite")
    public Result<List<String>> favorite(@PathVariable Long id, HttpSession session) {
        String username = requireLogin(session);
        return Result.ok(postService.toggleFavorite(username, id));
    }

    private String requireLogin(HttpSession session) {
        String username = (String) session.getAttribute(UserController.SESSION_USER);
        if (username == null) {
            throw new BizException("请先登录喵～");
        }
        return username;
    }
}
