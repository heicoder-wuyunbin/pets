package com.pets.stock.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.stock.domain.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
