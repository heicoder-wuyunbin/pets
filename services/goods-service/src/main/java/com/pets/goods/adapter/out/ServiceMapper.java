package com.pets.goods.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.goods.domain.entity.ServiceItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceMapper extends BaseMapper<ServiceItem> {
}
