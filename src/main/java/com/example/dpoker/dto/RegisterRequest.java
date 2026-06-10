package com.example.dpoker.dto;

import lombok.Data;

/**
 * 注册请求参数，包含注册所需的所有字段。
 */
@Data
public class RegisterRequest {
    private String username;     // 用户名（必填）
    private String password;     // 密码（必填）
    private String nickname;     // 昵称（选填，默认使用用户名）
    private String registerCode; // 注册码（必填，用于验证注册权限）
}