package com.pets.trade.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.trade.domain.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {
}
