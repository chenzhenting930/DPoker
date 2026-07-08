package com.example.dpoker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库user表
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;             // 主键
    private String username;     // 用户名
    private String password;     // 密码
    private float point; // 用户积分
    private LocalDateTime createTime; // 创建时间
    private String nickname;
    /**
     * 头像标识：格式 "preset:N"，N 为前端预设头像库的索引。
     * 前端 UserAvatar 据此渲染对应的 emoji 头像；
     * 为空时前端按 nickname hash 兜底选一个预设头像（避免出现字母头像）。
     */
    private String avatar;
    private Integer test; // 是否为测试账号 0:否 1:是
}
