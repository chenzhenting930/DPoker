package com.example.dpoker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 赛季排名实体，对应数据库 season_rank 表
 * ---------------------------------------------------------------------------
 * 赛季结算时把每个玩家的最终积分快照写入此表。
 * nickname/avatar 冗余存储，避免玩家后续改名/换头像导致历史赛季数据失真。
 *
 * 注意：rank 是 MySQL 8.0 保留字（窗口函数 RANK()），
 * 必须用 @TableField 加反引号包裹，否则生成的 SQL 会语法错误。
 */
@Data
@TableName("season_rank")
public class SeasonRank {
    @TableId(type = IdType.AUTO)
    private Integer id;             // 主键
    private Integer seasonId;       // 赛季ID（关联 season.id）
    private Integer userId;         // 用户ID
    private String nickname;        // 结算时的昵称（冗余快照）
    private String avatar;          // 结算时的头像（冗余快照）
    private Float finalPoint;       // 最终积分
    /** 名次（1, 2, 3...），列名 rank 是 MySQL 8.0 保留字，必须用反引号 */
    @TableField(value = "`rank`")
    private Integer rank;
}
