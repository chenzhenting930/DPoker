package com.example.dpoker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dpoker.Mapper.UserMapper;
import com.example.dpoker.Utils.LoginTokenManager;
import com.example.dpoker.dto.RegisterRequest;
import com.example.dpoker.dto.Result;
import com.example.dpoker.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 注册控制器，处理用户账号注册请求。
 * 注册码在 application.yaml 的 dpoker.register-code 中配置，匹配即可注册。
 */
@RestController
@Slf4j
public class RegisterController {

    /** 从配置文件读取注册码，修改配置文件即可更换 */
    @Value("${dpoker.register-code}")
    private String validRegisterCode;

    /**
     * 前端预设头像库大小（与 UserAvatar.vue 中 AVATAR_PRESETS 数组长度保持一致）。
     * 注册时随机分配一个 preset:N，让新用户一开始就有专属头像，避免出现字母头像。
     */
    private static final int PRESET_AVATAR_COUNT = 12;

    @Autowired
    UserMapper userMapper;

    /**
     * 用户注册接口。
     * 校验用户名是否重复、注册码是否匹配配置中的注册码，通过后创建用户并返回登录token。
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest request) {
        // 1. 基础参数校验
        if (request == null) {
            return Result.fail("请求参数不能为空");
        }
        String username = request.getUsername();
        String password = request.getPassword();
        String nickname = request.getNickname();
        String registerCode = request.getRegisterCode();

        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        if (registerCode == null || registerCode.trim().isEmpty()) {
            return Result.fail("注册码不能为空");
        }

        // 2. 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existUser != null) {
            return Result.fail("用户名已存在");
        }

        // 3. 校验注册码：直接与配置文件中的注册码比对
        if (!validRegisterCode.equals(registerCode)) {
            return Result.fail("注册码错误");
        }

        // 4. 创建用户：昵称默认为用户名
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname : username);
        newUser.setPoint(10000);          // 新用户赠送10000初始积分
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setTest(0); // 非测试账号
        // 随机分配一个预设头像，避免新用户出现字母头像
        newUser.setAvatar("preset:" + ThreadLocalRandom.current().nextInt(PRESET_AVATAR_COUNT));
        userMapper.insert(newUser);

        log.info("新用户注册成功: username={}, userId={}", username, newUser.getId());

        // 5. 生成登录token并返回，注册后直接免登录进入
        String token = LoginTokenManager.generateToken(newUser.getId());
        return Result.success(token);
    }
}