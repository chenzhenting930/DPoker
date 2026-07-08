package com.example.dpoker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dpoker.Mapper.UserMapper;
import com.example.dpoker.Utils.LoginTokenManager;
import com.example.dpoker.dto.Result;
import com.example.dpoker.dto.UpdateAvatarRequest;
import com.example.dpoker.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料控制器
 * ---------------------------------------------------------------------------
 * 处理用户自助修改资料的 HTTP 接口（与游戏 WebSocket 频道分离）。
 * 鉴权方式：请求头 token —— 由 LoginTokenManager 校验并解析出 userId。
 * 前端 axios 调用 /api/updateAvatar 时把 userStore.token 放进 header。
 */
@RestController
@Slf4j
public class UserController {

    /**
     * 前端预设头像库大小（与 UserAvatar.vue 中 AVATAR_PRESETS 数组长度保持一致）。
     * 用于校验 preset:N 的 N 范围。
     */
    private static final int PRESET_AVATAR_COUNT = 12;

    @Autowired
    UserMapper userMapper;

    /**
     * 更新当前登录用户的头像。
     * ---------------------------------------------------------------------------
     * 流程：
     *   1) 从 header 取 token，用 LoginTokenManager 拿到 userId
     *   2) 校验请求体 avatar 格式（"preset:N"，N 在 0~PRESET_AVATAR_COUNT-1 范围内）
     *   3) 落库，返回最新 User 对象让前端同步本地状态
     *
     * @param token   请求头中的登录 token
     * @param request 请求体 { avatar: "preset:N" }
     * @return Result.data 为更新后的 User
     */
    @PostMapping("/updateAvatar")
    public Result updateAvatar(
            @RequestHeader(value = "token", required = false) String token,
            @RequestBody UpdateAvatarRequest request) {

        // 1) 鉴权：token 缺失或不合法 → 401
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }
        Integer userId = LoginTokenManager.validateToken(token);
        if (userId == null) {
            return Result.fail("登录已失效，请重新登录");
        }

        // 2) 查用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<>(User.class).eq(User::getId, userId));
        if (user == null) {
            return Result.fail("用户不存在");
        }

        // 3) 校验 avatar 格式：必须形如 "preset:N"，且 N 在合法范围
        //    这层校验防止前端传非法值导致前端渲染时索引越界。
        String avatar = request == null ? null : request.getAvatar();
        if (avatar == null || !avatar.startsWith("preset:")) {
            return Result.fail("头像格式不合法");
        }
        int idx;
        try {
            idx = Integer.parseInt(avatar.substring("preset:".length()));
        } catch (NumberFormatException e) {
            return Result.fail("头像格式不合法");
        }
        if (idx < 0 || idx >= PRESET_AVATAR_COUNT) {
            return Result.fail("头像范围不合法");
        }

        // 4) 落库并返回最新 User（前端据此更新 userStore，无需重新登录）
        user.setAvatar(avatar);
        userMapper.updateById(user);
        log.info("用户 {} 更新头像为 {}", userId, avatar);
        return Result.success("头像更新成功", user);
    }
}
