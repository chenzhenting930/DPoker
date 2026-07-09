package com.example.dpoker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 赛季实体，对应数据库 season 表
 * ---------------------------------------------------------------------------
 * 每个赛季一条记录。status='active' 的为当前进行中的赛季（全表唯一）；
 * 结算后该赛季 status 改为 'ended'，同时创建下一条 active 赛季。
 */
@Data
@TableName("season")
public class Season {
    @TableId(type = IdType.AUTO)
    private Integer id;             // 主键
    private Integer seasonNumber;   // 赛季编号（1, 2, 3...），便于人类阅读
    private LocalDateTime startTime; // 赛季开始时间
    private LocalDateTime endTime;   // 赛季结束时间（当前赛季为 NULL）
    private String status;          // 状态：active=进行中 / ended=已结束
}
