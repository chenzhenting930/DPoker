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
}
