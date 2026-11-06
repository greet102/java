package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fenglin.springboottest.entity.Notification;
import com.fenglin.springboottest.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知：点赞 / 收藏 / 评论 / 关注 时给作者发通知，
 * 用户注销、文章删除时级联清理。
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** 通知面板最多展示条数 */
    private static final int RECENT_LIMIT = 50;

    private final NotificationMapper notificationMapper;

    /**
     * 写入一条通知
     *
     * @param username 接收者
     * @param actor    触发者
     * @param type     like / favorite / comment / follow
     * @param postId   相关文章ID（follow 传 null）
     * @param postTitle 文章标题快照
     * @param content  评论摘要（仅 comment 类型，其余传 null）
     */
    public void notify(String username, String actor, String type,
                       Long postId, String postTitle, String content) {
        if (username == null || actor == null || username.equals(actor)) {
            // 自己给自己点赞/收藏/评论/关注不发通知
            return;
        }
        Notification n = new Notification();
        n.setUsername(username);
        n.setActor(actor);
        n.setType(type);
        n.setPostId(postId);
        n.setPostTitle(postTitle);
        n.setContent(content);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    /** 当前用户最近的通知（新的在前，最多 50 条） */
    public List<Notification> listRecent(String username) {
        return notificationMapper.selectList(
                new QueryWrapper<Notification>()
                        .eq("username", username)
                        .orderByDesc("created_at")
                        .orderByDesc("id")
                        .last("LIMIT " + RECENT_LIMIT));
    }

    /** 当前用户未读通知数 */
    public long unreadCount(String username) {
        return notificationMapper.selectCount(
                new QueryWrapper<Notification>()
                        .eq("username", username)
                        .eq("is_read", 0));
    }

    /** 当前用户全部标记为已读 */
    public void markAllRead(String username) {
        Notification update = new Notification();
        update.setIsRead(true);
        notificationMapper.update(update,
                new UpdateWrapper<Notification>()
                        .eq("username", username)
                        .eq("is_read", 0));
    }

    /** 用户注销时删除其收到的通知 */
    public void deleteByUser(String username) {
        notificationMapper.delete(
                new QueryWrapper<Notification>().eq("username", username));
    }

    /** 用户注销时删除其触发的通知 */
    public void deleteByActor(String username) {
        notificationMapper.delete(
                new QueryWrapper<Notification>().eq("actor", username));
    }

    /** 文章删除时删除相关通知 */
    public void deleteByPost(Long postId) {
        notificationMapper.delete(
                new QueryWrapper<Notification>().eq("post_id", postId));
    }
}
