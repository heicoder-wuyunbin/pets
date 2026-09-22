package com.pets.trade.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.trade.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
