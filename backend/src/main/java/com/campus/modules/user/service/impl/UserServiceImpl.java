package com.campus.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.mapper.UserMapper;
import com.campus.modules.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User getByPhone(String phone) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return baseMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public User register(String phone, String password, String nickname) {
        if (existsByPhone(phone)) {
            throw new IllegalArgumentException("手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setGender(0);
        user.setStatus(1);

        baseMapper.insert(user);
        return user;
    }

    @Override
    public void updateProfile(Long userId, String nickname, Integer gender, String bio, String avatar) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        baseMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long userId, String nickname, Integer gender, String bio, String avatar, String grade, String major) {
        System.out.println("=== DEBUG UserServiceImpl === userId=" + userId + ", nickname=" + nickname + ", gender=" + gender);
        
        User user = baseMapper.selectById(userId);
        System.out.println("=== DEBUG UserServiceImpl === selectById result: " + (user == null ? "null" : "user found"));
        
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (grade != null) {
            user.setGrade(grade);
        }
        if (major != null) {
            user.setMajor(major);
        }

        baseMapper.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        baseMapper.updateById(user);
    }
}
