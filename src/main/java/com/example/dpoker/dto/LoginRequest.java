package com.example.dpoker.dto;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    private String username; // 用户名
    private String password; // 密码
}
