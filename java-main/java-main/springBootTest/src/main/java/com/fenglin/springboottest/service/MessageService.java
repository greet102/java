package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.ConversationVO;
import com.fenglin.springboottest.dto.MessageVO;
import com.fenglin.springboottest.entity.Message;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.entity.UserFollow;
import com.fenglin.springboottest.mapper.FollowMapper;
import com.fenglin.springboottest.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信业务：收发私信、会话列表、未读数、标记已读。
 *
 * 核心规则（给已关注的用户增加私信功能）：
 *   - 发起私信必须【已关注对方】；
 *   - 一旦两人有过会话（任何方向），对方可以正常回复（hasThread 放行）。
 *   这样既能保证“私信只面向已关注的用户”，又不至于让对方无法回话。
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final FollowMapper followMapper;
    private final UserService userService;

    private static final int MAX_LEN = 1000;

    /**
     * 发送一条私信
     */
    public MessageVO send(String sender, String receiver, String content) {
        if (sender.equals(receiver)) {
            throw new BizException("不能给自己发私信喵～");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BizException("私信内容不能为空喵～");
        }
        content = content.trim();
        if (content.length() > MAX_LEN) {
            throw new BizException("私信太长了，最多 " + MAX_LEN + " 字喵～");
        }
        User target = userService.getUser(receiver);
        if (target == null) {
            throw new BizException("对方用户不存在喵～");
        }
        // 关注校验：必须关注对方，或两人已有过会话（允许回复）
        if (!follows(sender, receiver) && !hasThread(sender, receiver)) {
            throw new BizException("只能给已关注的用户发私信喵～");
        }

        Message m = new Message();
        m.setSender(sender);
        m.setReceiver(receiver);
        m.setContent(content);
        m.setIsRead(false);
        m.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(m);
        return toVO(m);
    }

    /**
     * 拉取与某人的全部私信（按时间正序），并把来自对方的未读标记为已读
     */
    public List<MessageVO> thread(String me, String other) {
        List<Message> list = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .nested(w -> w.eq("sender", me).eq("receiver", other))
                        .or(w -> w.eq("sender", other).eq("receiver", me))
                        .orderByAsc("created_at"));
        markRead(me, other);
        List<MessageVO> res = new ArrayList<>();
        for (Message m : list) res.add(toVO(m));
        return res;
    }

    /**
     * 会话列表：与每个人的最近一条消息 + 来自对方的未读数（按最近活跃倒序）
     */
    public List<ConversationVO> conversations(String me) {
        List<Message> all = messageMapper.selectList(
                new QueryWrapper<Message>()
                        .and(w -> w.eq("sender", me).or().eq("receiver", me))
                        .orderByDesc("created_at"));

        // 用插入顺序的 Map 保证“每个对方一条”，且首条即最新消息
        Map<String, ConversationVO> map = new LinkedHashMap<>();
        for (Message m : all) {
            String other = m.getSender().equals(me) ? m.getReceiver() : m.getSender();
            ConversationVO c = map.get(other);
            if (c == null) {
                User u = userService.getUser(other);
                c = new ConversationVO();
                c.setUsername(other);
                c.setNickname(u != null && u.getNickname() != null ? u.getNickname() : other);
                c.setAvatar(u != null && u.getAvatar() != null ? u.getAvatar() : "🐱");
                c.setUnread(0);
                map.put(other, c);
            }
            // 仅首条（最新）写入摘要
            if (c.getLastContent() == null) {
                c.setLastContent(m.getContent());
                c.setLastTime(toMillis(m.getCreatedAt()));
            }
            // 累计来自对方的未读
            if (m.getReceiver().equals(me) && !m.getIsRead()) {
                c.setUnread(c.getUnread() + 1);
            }
        }
        return new ArrayList<>(map.values());
    }

    /** 当前用户的未读私信总数 */
    public int unread(String me) {
        return Math.toIntExact(messageMapper.selectCount(
                new QueryWrapper<Message>().eq("receiver", me).eq("is_read", 0)));
    }

    /** 把某人发来的私信标记为已读 */
    public void markRead(String me, String other) {
        Message upd = new Message();
        upd.setIsRead(true);
        messageMapper.update(upd, new QueryWrapper<Message>()
                .eq("receiver", me).eq("sender", other).eq("is_read", 0));
    }

    // ---------- 内部工具 ----------

    private boolean follows(String a, String b) {
        return followMapper.selectCount(new QueryWrapper<UserFollow>()
                .eq("follower", a).eq("following", b)) > 0;
    }

    private boolean hasThread(String a, String b) {
        return messageMapper.selectCount(new QueryWrapper<Message>()
                .nested(w -> w.eq("sender", a).eq("receiver", b))
                .or(w -> w.eq("sender", b).eq("receiver", a))) > 0;
    }

    private MessageVO toVO(Message m) {
        MessageVO v = new MessageVO();
        v.setId(m.getId());
        v.setSender(m.getSender());
        v.setReceiver(m.getReceiver());
        v.setContent(m.getContent());
        v.setIsRead(m.getIsRead() != null && m.getIsRead());
        v.setCreatedAt(toMillis(m.getCreatedAt()));
        return v;
    }

    private long toMillis(LocalDateTime t) {
        if (t == null) return 0;
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
