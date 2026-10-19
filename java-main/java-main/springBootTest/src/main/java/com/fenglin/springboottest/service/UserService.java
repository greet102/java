package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户相关业务：注册、登录
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

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

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
