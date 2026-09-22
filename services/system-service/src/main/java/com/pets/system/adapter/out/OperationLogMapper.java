package com.pets.system.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.system.domain.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
