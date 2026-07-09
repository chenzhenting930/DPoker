package com.example.dpoker.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dpoker.entity.Season;
import org.apache.ibatis.annotations.Mapper;

/**
 * 赛季Mapper，MyBatisPlus自动实现CRUD
 */
@Mapper
public interface SeasonMapper extends BaseMapper<Season> {
}
