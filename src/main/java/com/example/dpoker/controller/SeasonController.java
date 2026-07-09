package com.example.dpoker.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.dpoker.Utils.LoginTokenManager;
import com.example.dpoker.dto.Result;
import com.example.dpoker.entity.Season;
import com.example.dpoker.entity.SeasonRank;
import com.example.dpoker.entity.User;
import com.example.dpoker.Mapper.SeasonMapper;
import com.example.dpoker.Mapper.SeasonRankMapper;
import com.example.dpoker.Mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛季管理接口
 * ---------------------------------------------------------------------------
 * 三个接口（前端通过 Vite 代理 /api → 后端根路径，所以这里不带 /api 前缀）：
 *   1. POST /seasonSettle   赛季结算（仅用户 zy 有权限）
 *   2. GET  /seasonList      获取所有赛季列表（用于排行榜切换查看）
 *   3. GET  /seasonRank      获取某赛季排名（seasonId=0 表示当前赛季）
 *
 * 结算逻辑：
 *   a. 快照当前所有真实玩家（test=0）按积分降序排名写入 season_rank
 *   b. 当前赛季 end_time=now, status='ended'
 *   c. 创建新赛季 season_number+1, status='active'
 *   d. 所有真实玩家积分重置为 10000
 *   全程 @Transactional 保证原子性，任意一步失败则整体回滚
 */
@Slf4j
@RestController
public class SeasonController {

    @Autowired
    private SeasonMapper seasonMapper;
    @Autowired
    private SeasonRankMapper seasonRankMapper;
    @Autowired
    private UserMapper userMapper;

    /** 赛季重置后的初始积分 */
    private static final float SEASON_RESET_POINT = 10000f;
    /** 拥有赛季结算权限的用户名 */
    private static final String SEASON_ADMIN_USERNAME = "zy";

    /**
     * 赛季结算
     * ---------------------------------------------------------------------------
     * 仅用户 zy 可调用。流程：
     *   1. 查当前 active 赛季
     *   2. 查所有真实玩家按 point 降序
     *   3. 逐条写入 season_rank（含 nickname/avatar 快照）
     *   4. 当前赛季 end_time=now, status='ended'
     *   5. 创建新赛季 season_number+1, status='active'
     *   6. 所有真实玩家 point=10000
     */
    @PostMapping("/seasonSettle")
    @Transactional
    public Result settle(@RequestHeader(value = "token", required = false) String token) {
        // 注：路径不带 /api 前缀，因为 Vite 代理已 rewrite 去掉 /api
        // 1. 鉴权：校验 token + 用户名必须是 zy
        Integer userId = LoginTokenManager.validateToken(token);
        if (userId == null) {
            return Result.fail(401, "登录已失效，请重新登录", null);
        }
        User operator = userMapper.selectById(userId);
        if (operator == null || !SEASON_ADMIN_USERNAME.equals(operator.getUsername())) {
            return Result.fail("无权限：仅用户 zy 可进行赛季结算");
        }

        // 2. 查当前进行中的赛季
        Season currentSeason = seasonMapper.selectOne(
                new QueryWrapper<Season>().eq("status", "active"));
        if (currentSeason == null) {
            return Result.fail("未找到进行中的赛季，无法结算");
        }

        // 3. 查所有真实玩家按积分降序（与排行榜 getPointRank 保持一致：只含 test=0）
        List<User> users = userMapper.selectList(
                new QueryWrapper<User>()
                        .eq("test", 0)
                        .orderByDesc("point"));

        if (users.isEmpty()) {
            return Result.fail("没有真实玩家，无需结算");
        }

        // 4. 逐条写入赛季排名快照
        //    rank 从 1 开始递增；同分玩家按查询顺序并列（简化处理，不做 tie-break）
        int rank = 1;
        for (User u : users) {
            SeasonRank sr = new SeasonRank();
            sr.setSeasonId(currentSeason.getId());
            sr.setUserId(u.getId());
            sr.setNickname(u.getNickname());
            sr.setAvatar(u.getAvatar());
            sr.setFinalPoint(u.getPoint());
            sr.setRank(rank++);
            seasonRankMapper.insert(sr);
        }

        // 5. 当前赛季标记为已结束
        Season updateSeason = new Season();
        updateSeason.setId(currentSeason.getId());
        updateSeason.setEndTime(LocalDateTime.now());
        updateSeason.setStatus("ended");
        seasonMapper.updateById(updateSeason);

        // 6. 创建新赛季
        Season nextSeason = new Season();
        nextSeason.setSeasonNumber(currentSeason.getSeasonNumber() + 1);
        nextSeason.setStartTime(LocalDateTime.now());
        nextSeason.setStatus("active");
        seasonMapper.insert(nextSeason);

        // 7. 所有真实玩家积分重置为 10000
        userMapper.update(null,
                new UpdateWrapper<User>()
                        .eq("test", 0)
                        .set("point", SEASON_RESET_POINT));

        log.info("赛季结算完成：赛季 {} 已归档，新赛季 {} 开始", currentSeason.getSeasonNumber(), nextSeason.getSeasonNumber());

        // 8. 返回结算摘要（前端可用于弹窗展示）
        Map<String, Object> summary = new HashMap<>();
        summary.put("settledSeasonNumber", currentSeason.getSeasonNumber());
        summary.put("nextSeasonNumber", nextSeason.getSeasonNumber());
        summary.put("playerCount", users.size());
        return Result.success("赛季结算成功", summary);
    }

    /**
     * 获取所有赛季列表（按编号降序，当前赛季在最前）
     * ---------------------------------------------------------------------------
     * 前端排行榜弹窗用此接口渲染赛季切换下拉。
     * 当前赛季（active）也会返回，选择"当前赛季"时调 seasonRank?seasonId=0 即可。
     */
    @GetMapping("/seasonList")
    public Result seasonList(@RequestHeader(value = "token", required = false) String token) {
        // 注：路径不带 /api 前缀，因为 Vite 代理已 rewrite 去掉 /api
        Integer userId = LoginTokenManager.validateToken(token);
        if (userId == null) {
            return Result.fail(401, "登录已失效，请重新登录", null);
        }
        List<Season> seasons = seasonMapper.selectList(
                new QueryWrapper<Season>().orderByDesc("season_number"));
        return Result.success("获取赛季列表成功", seasons);
    }

    /**
     * 获取某赛季排名
     * ---------------------------------------------------------------------------
     * @param seasonId 赛季ID；传 0 或不传表示当前赛季（直接查 user 表实时积分）
     *                 传具体ID则查 season_rank 历史快照
     */
    @GetMapping("/seasonRank")
    public Result seasonRank(
            @RequestHeader(value = "token", required = false) String token,
            @RequestParam(defaultValue = "0") Integer seasonId) {
        // 注：路径不带 /api 前缀，因为 Vite 代理已 rewrite 去掉 /api
        Integer userId = LoginTokenManager.validateToken(token);
        if (userId == null) {
            return Result.fail(401, "登录已失效，请重新登录", null);
        }

        // seasonId=0：当前赛季，直接查 user 表实时积分（与 getPointRank 逻辑一致）
        if (seasonId == null || seasonId == 0) {
            List<User> users = userMapper.selectList(
                    new QueryWrapper<User>()
                            .select("nickname", "point", "avatar")
                            .eq("test", 0)
                            .orderByDesc("point"));
            return Result.success("获取当前赛季排名成功", users);
        }

        // 历史赛季：查 season_rank 快照
        List<SeasonRank> ranks = seasonRankMapper.selectList(
                new QueryWrapper<SeasonRank>()
                        .eq("season_id", seasonId)
                        .orderByAsc("rank"));
        return Result.success("获取历史赛季排名成功", ranks);
    }
}
