package com.example.dpoker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dpoker.Mapper.UserMapper;
import com.example.dpoker.Utils.LoginTokenManager;
import com.example.dpoker.dto.ActionRequest;
import com.example.dpoker.dto.GameResponse;
import com.example.dpoker.dto.LoginRequest;
import com.example.dpoker.dto.Result;
import com.example.dpoker.entity.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class LoginController {
    @Autowired
    UserMapper userMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest){
        if (loginRequest == null){
            return Result.fail("用户名或密码不能为空");
        }
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        User user = userMapper.selectOne(new LambdaQueryWrapper<>(User.class).eq(User::getUsername, username));
        if (user == null){
            return Result.fail("用户不存在");
        }
        if (!user.getPassword().equals(password)){
            return Result.fail("密码错误");
        }
        String token = LoginTokenManager.generateToken(user.getId());
        return Result.success(token);
    }

    @MessageMapping("/getUserInfo")
    @SendToUser("/queue")
    public Result handlePlayerAction(StompHeaderAccessor accessor)  {
        Integer userId = (Integer) accessor.getSessionAttributes().get("userId");
        User user = userMapper.selectOne(new LambdaQueryWrapper<>(User.class).eq(User::getId,userId));
        return Result.success(user);
    }



}
