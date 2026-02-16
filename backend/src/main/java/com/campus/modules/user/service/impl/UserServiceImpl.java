package com.campus.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
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
    @DS("slave")
    public User getByPhone(String phone) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    @DS("slave")
    public boolean existsByPhone(String phone) {
        return baseMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public User register(String phone, String password, String nickname) {
        // 先检查是否存在（包括已删除的）
        User existingUser = getByPhoneIncludingDeleted(phone);
        if (existingUser != null) {
            if (existingUser.getDeleted() != null && existingUser.getDeleted() == 1) {
                // 用户已删除，更新deleted=0状态（使用原生SQL绕过逻辑删除）
                existingUser.setDeleted(0);
                existingUser.setPassword(passwordEncoder.encode(password));
                if (nickname != null && !nickname.isEmpty()) {
                    existingUser.setNickname(nickname);
                }
                existingUser.setStatus(1);
                // 先更新deleted状态
                baseMapper.updateDeletedStatus(existingUser.getId(), 0);
                // 再更新其他字段
                baseMapper.updateById(existingUser);
                return existingUser;
            } else {
                // 用户未删除，已存在
                throw new IllegalArgumentException("手机号已注册");
            }
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setGender(0);
        user.setStatus(1);
        user.setDeleted(0);

        baseMapper.insert(user);
        return user;
    }

    @Override
    public void updateProfile(Long userId, String nickname, Integer gender, String bio, String avatar, String grade, String major) {
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

    @Override
    public User getByPhoneIncludingDeleted(String phone) {
        // 使用原生SQL查询，忽略逻辑删除
        return baseMapper.selectByPhoneIncludingDeleted(phone);
    }

    @Override
    public void reactivateUser(Long userId, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (user.getDeleted() == null || user.getDeleted() != 1) {
            throw new IllegalArgumentException("该用户未被删除，无需重新激活");
        }
        // 重新激活：设置deleted=0，更新密码
        user.setDeleted(0);
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        baseMapper.updateById(user);
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 软删除用户（使用原生SQL绕过逻辑删除）
        baseMapper.updateDeletedStatus(userId, 1);
    }
}
