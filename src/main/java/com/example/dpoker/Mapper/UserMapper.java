package com.example.dpoker.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dpoker.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper，MyBatisPlus自动实现CRUD
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
