package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.ProfileVO;
import com.fenglin.springboottest.dto.UserSummary;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.entity.UserFollow;
import com.fenglin.springboottest.mapper.FollowMapper;
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

    private String str(Object o) {
        return o == null ? null : o.toString();
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
