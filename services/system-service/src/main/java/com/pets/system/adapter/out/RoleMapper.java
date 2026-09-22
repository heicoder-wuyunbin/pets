package com.pets.system.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.system.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
