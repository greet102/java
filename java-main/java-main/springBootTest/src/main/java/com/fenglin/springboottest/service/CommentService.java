package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.CommentVO;
import com.fenglin.springboottest.entity.Comment;
import com.fenglin.springboottest.entity.Post;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.CommentMapper;
import com.fenglin.springboottest.mapper.PostMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论业务：查看 / 发表 / 删除
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    /** 某篇文章的评论列表（时间正序，最早的在前） */
    public List<CommentVO> listByPost(Long postId) {
        List<Comment> comments = commentMapper.selectList(
                new QueryWrapper<Comment>()
                        .eq("post_id", postId)
                        .orderByAsc("created_at"));
        // 批量查出评论者信息，避免 N 次查询
        Map<String, User> users = batchUsers(comments.stream()
                .map(Comment::getUsername)
                .collect(Collectors.toSet()));
        return comments.stream()
                .map(c -> toVO(c, users.get(c.getUsername())))
                .collect(Collectors.toList());
    }

    /** 发表评论 */
    @Transactional
    public CommentVO add(String username, Long postId, String content) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException("文章不见了喵");
        }
        content = content == null ? "" : content.trim();
        if (content.isEmpty()) {
            throw new BizException("评论内容不能为空哦～");
        }
        if (content.length() > 500) {
            throw new BizException("评论最多 500 字喵");
        }
        Comment c = new Comment();
        c.setPostId(postId);
        c.setUsername(username);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(c);
        return toVO(c, userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)));
    }

    /**
     * 删除评论：评论作者本人或已登录后台的管理员可删
     */
    @Transactional
    public void delete(String username, Long commentId, boolean isAdmin) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) {
            throw new BizException("评论不存在喵");
        }
        if (!isAdmin && !c.getUsername().equals(username)) {
            throw new BizException("只能删除自己的评论喵～");
        }
        commentMapper.deleteById(commentId);
    }

    /** 级联清理：删除一篇文章时删掉它的全部评论 */
    public void deleteByPost(Long postId) {
        commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", postId));
    }

    /** 级联清理：删除一个用户时，删掉 TA 发表的全部评论 */
    public void deleteByUser(String username) {
        commentMapper.delete(new QueryWrapper<Comment>().eq("username", username));
    }

    /** 统计一批文章的评论数：postId -> 评论数 */
    public Map<Long, Long> countByPosts(java.util.Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        return commentMapper.selectList(new QueryWrapper<Comment>().in("post_id", postIds))
                .stream()
                .collect(Collectors.groupingBy(Comment::getPostId, Collectors.counting()));
    }

    // ==================== 私有工具 ====================

    private CommentVO toVO(Comment c, User u) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setUsername(c.getUsername());
        vo.setContent(c.getContent());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setNickname(u == null ? null : u.getNickname());
        vo.setAvatar(u == null ? null : u.getAvatar());
        return vo;
    }

    private Map<String, User> batchUsers(java.util.Set<String> usernames) {
        if (usernames.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new QueryWrapper<User>().in("username", usernames))
                .stream()
                .collect(Collectors.toMap(User::getUsername, u -> u, (a, b) -> a));
    }
}
