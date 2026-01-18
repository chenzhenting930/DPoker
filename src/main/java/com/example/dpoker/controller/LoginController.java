package com.example.dpoker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dpoker.Mapper.UserMapper;
import com.example.dpoker.Utils.LoginTokenManager;
import com.example.dpoker.dto.LoginRequest;
import com.example.dpoker.dto.Result;
import com.example.dpoker.entity.User;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {
    @Autowired
    UserMapper userMapper;

    @PostMapping("/login")
    public Result<Object> login(@RequestBody LoginRequest loginRequest){
        if (loginRequest == null){
            return Result.fail(404,"用户名或密码不能为空");
        }
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        User user = userMapper.selectOne(new LambdaQueryWrapper<>(User.class).eq(User::getUsername, username));
        if (user == null){
            return Result.fail(404,"用户不存在");
        }
        if (!user.getPassword().equals(password)){
            return Result.fail(404,"密码错误");
        }
        Map<String, Object> result = Map.of("user", user, "token", LoginTokenManager.generateToken(user.getId()));
        return Result.success(result);
    }
}
