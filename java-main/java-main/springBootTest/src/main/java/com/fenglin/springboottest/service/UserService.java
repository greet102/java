package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.AdminStatsVO;
import com.fenglin.springboottest.dto.ProfileVO;
import com.fenglin.springboottest.dto.UserSummary;
import com.fenglin.springboottest.entity.Message;
import com.fenglin.springboottest.entity.Post;
import com.fenglin.springboottest.entity.PostFavorite;
import com.fenglin.springboottest.entity.PostLike;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.entity.UserFollow;
import com.fenglin.springboottest.mapper.FollowMapper;
import com.fenglin.springboottest.mapper.MessageMapper;
import com.fenglin.springboottest.mapper.PostFavoriteMapper;
import com.fenglin.springboottest.mapper.PostLikeMapper;
import com.fenglin.springboottest.mapper.PostMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户相关业务：注册、登录、个人主页、关注
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final MessageMapper messageMapper;

    /**
     * 注册（成功后由控制器写入会话，实现自动登录）
     */
    public User register(String username, String password) {
        username = trim(username);
        if (username.isEmpty() || password == null || password.isEmpty()) {
            throw new BizException("用户名和密码都不能空哦～");
        }
        if (password.length() < 4) {
            throw new BizException("密码至少要 4 位喵");
        }
        Long count = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", username));
        if (count != null && count > 0) {
            throw new BizException("这个用户名已经被占用了");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 演示项目，明文存储
        user.setAvatar("🐱");
        user.setNickname(username);
        user.setSlogan("这个人很懒，什么也没写～");
        user.setBio("");
        user.setLocation("");
        userMapper.insert(user);
        return user;
    }

    /**
     * 登录校验，通过返回用户对象
     */
    public User login(String username, String password) {
        username = trim(username);
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (user == null || !user.getPassword().equals(password)) {
            throw new BizException("用户名或密码不对喵～");
        }
        return user;
    }

    /** 按用户名查询用户 */
    public User getUser(String username) {
        if (username == null) return null;
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    /**
     * 查询个人主页信息。
     *
     * @param viewer 当前登录用户（可能为 null，表示未登录）
     * @param target 被查看的用户名
     */
    public ProfileVO getProfile(String viewer, String target) {
        User u = getUser(target);
        if (u == null) {
            throw new BizException("用户不存在喵～");
        }
        ProfileVO vo = new ProfileVO();
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setSlogan(u.getSlogan());
        vo.setBio(u.getBio());
        vo.setAvatar(u.getAvatar());
        vo.setLocation(u.getLocation());
        vo.setBirthday(u.getBirthday());

        long followerCount = followMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("following", target));
        long followingCount = followMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("follower", target));
        vo.setFollowerCount(followerCount);
        vo.setFollowingCount(followingCount);

        boolean isFollowing = viewer != null
                && followMapper.selectCount(new QueryWrapper<UserFollow>()
                        .eq("follower", viewer).eq("following", target)) > 0;
        vo.setFollowing(isFollowing);

        vo.setFollowings(getFollowings(viewer, target));
        return vo;
    }

    /**
     * 查询某人关注的用户列表（用于“我的关注” / 主页的“TA 关注的人”）。
     * 同时按浏览者是否已关注，填充每行的 isFollowing，供前端 UserRow 显示“已关注”。
     */
    public List<UserSummary> getFollowings(String viewer, String username) {
        List<UserFollow> list = followMapper.selectList(
                new QueryWrapper<UserFollow>().eq("follower", username));
        // 浏览者已关注的人，一次查出后放进集合，避免 N 次查询
        Set<String> myFollowed = new HashSet<>();
        if (viewer != null) {
            List<UserFollow> mine = followMapper.selectList(
                    new QueryWrapper<UserFollow>().eq("follower", viewer));
            for (UserFollow f : mine) myFollowed.add(f.getFollowing());
        }
        List<UserSummary> res = new ArrayList<>();
        for (UserFollow f : list) {
            User u = getUser(f.getFollowing());
            if (u == null) continue;
            UserSummary s = new UserSummary();
            s.setUsername(u.getUsername());
            s.setNickname(u.getNickname());
            s.setSlogan(u.getSlogan());
            s.setAvatar(u.getAvatar());
            s.setFollowing(viewer != null && myFollowed.contains(u.getUsername()));
            res.add(s);
        }
        return res;
    }

    /**
     * 关注 / 取消关注，返回最新关注状态与粉丝数
     */
    public Map<String, Object> toggleFollow(String follower, String target) {
        QueryWrapper<UserFollow> w = new QueryWrapper<UserFollow>()
                .eq("follower", follower).eq("following", target);
        UserFollow existing = followMapper.selectOne(w);
        Map<String, Object> data = new HashMap<>();
        if (existing != null) {
            followMapper.delete(w);
            data.put("following", false);
        } else {
            UserFollow f = new UserFollow();
            f.setFollower(follower);
            f.setFollowing(target);
            f.setCreatedAt(LocalDateTime.now());
            followMapper.insert(f);
            data.put("following", true);
        }
        long followerCount = followMapper.selectCount(
                new QueryWrapper<UserFollow>().eq("following", target));
        data.put("followerCount", followerCount);
        return data;
    }

    /** 更新自己的个人资料 */
    public void updateProfile(String username, Map<String, Object> form) {
        User u = getUser(username);
        if (u == null) {
            throw new BizException("用户不存在喵～");
        }
        if (form.containsKey("nickname")) u.setNickname(str(form.get("nickname")));
        if (form.containsKey("slogan")) u.setSlogan(str(form.get("slogan")));
        if (form.containsKey("bio")) u.setBio(str(form.get("bio")));
        if (form.containsKey("avatar")) u.setAvatar(str(form.get("avatar")));
        if (form.containsKey("location")) u.setLocation(str(form.get("location")));
        if (form.containsKey("birthday")) {
            String b = str(form.get("birthday"));
            if (b == null || b.isEmpty()) {
                u.setBirthday(null);
            } else {
                try {
                    u.setBirthday(LocalDate.parse(b));
                } catch (Exception ignored) {
                    // 日期格式不对就忽略，保持原值
                }
            }
        }
        userMapper.updateById(u);
    }

    // ===================== 后台管理（管理员对前台用户数据的维护） =====================

    /** 数据概览：用户数、文章数、关注关系数、今日新增用户 */
    public AdminStatsVO adminStats() {
        AdminStatsVO vo = new AdminStatsVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setPostCount(postMapper.selectCount(null));
        vo.setFollowCount(followMapper.selectCount(null));
        long today = userMapper.selectCount(
                new QueryWrapper<User>().apply("DATE(created_at) = CURDATE()"));
        vo.setTodayUserCount(today);
        return vo;
    }

    /** 用户分页列表，支持按用户名 / 昵称模糊搜索，按注册时间倒序（手动分页，避免依赖分页插件） */
    public Map<String, Object> adminListUsers(String keyword, long page, long size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100;
        QueryWrapper<User> w = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like("username", keyword).or().like("nickname", keyword);
        }
        w.orderByDesc("created_at");
        long total = userMapper.selectCount(w);
        long offset = (page - 1) * size;
        // 复用同一查询条件，仅追加 LIMIT 做分页
        w.last("LIMIT " + offset + ", " + size);
        List<User> records = userMapper.selectList(w);
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("current", page);
        data.put("size", size);
        return data;
    }

    /** 编辑用户资料，password 字段为空则不修改密码 */
    public void adminUpdateUser(String username, Map<String, Object> form) {
        User u = getUser(username);
        if (u == null) {
            throw new BizException("用户不存在喵～");
        }
        if (form.containsKey("nickname")) u.setNickname(str(form.get("nickname")));
        if (form.containsKey("slogan")) u.setSlogan(str(form.get("slogan")));
        if (form.containsKey("bio")) u.setBio(str(form.get("bio")));
        if (form.containsKey("avatar")) u.setAvatar(str(form.get("avatar")));
        if (form.containsKey("location")) u.setLocation(str(form.get("location")));
        if (form.containsKey("password")) {
            String pw = str(form.get("password"));
            if (pw != null && !pw.isEmpty()) u.setPassword(pw);
        }
        userMapper.updateById(u);
    }

    /** 删除用户，并级联清理其文章、点赞、收藏、关注关系、私信，避免脏数据 */
    public void adminDeleteUser(String username) {
        User u = getUser(username);
        if (u == null) {
            throw new BizException("用户不存在喵～");
        }
        // 1) 该用户发布的文章：先删文章的点赞 / 收藏，再删文章
        List<Post> posts = postMapper.selectList(new QueryWrapper<Post>().eq("author", username));
        for (Post post : posts) {
            postLikeMapper.delete(new QueryWrapper<PostLike>().eq("post_id", post.getId()));
            postFavoriteMapper.delete(new QueryWrapper<PostFavorite>().eq("post_id", post.getId()));
        }
        postMapper.delete(new QueryWrapper<Post>().eq("author", username));
        // 2) 该用户发出的点赞 / 收藏
        postLikeMapper.delete(new QueryWrapper<PostLike>().eq("username", username));
        postFavoriteMapper.delete(new QueryWrapper<PostFavorite>().eq("username", username));
        // 3) 关注关系（作为关注者与被关注者）
        followMapper.delete(new QueryWrapper<UserFollow>().eq("follower", username));
        followMapper.delete(new QueryWrapper<UserFollow>().eq("following", username));
        // 4) 私信（收发）
        messageMapper.delete(new QueryWrapper<Message>().eq("sender", username));
        messageMapper.delete(new QueryWrapper<Message>().eq("receiver", username));
        // 5) 用户本身
        userMapper.deleteById(u.getId());
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
