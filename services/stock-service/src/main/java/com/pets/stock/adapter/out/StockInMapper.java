package com.pets.stock.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.stock.domain.entity.StockIn;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockInMapper extends BaseMapper<StockIn> {
}
