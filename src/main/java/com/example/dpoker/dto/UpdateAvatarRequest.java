package com.example.dpoker.dto;

import lombok.Data;

/**
 * 更新头像请求体
 * ---------------------------------------------------------------------------
 * 字段 avatar 形如 "preset:0"——前端预设头像库的索引。
 * 后端只做格式校验并落库，不解析具体含义；
 * 实际渲染由前端 UserAvatar 根据 avatar 字段完成。
 */
@Data
public class UpdateAvatarRequest {
    /** 头像标识，格式 "preset:N" */
    private String avatar;
}
