package com.pets.auth.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.auth.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
