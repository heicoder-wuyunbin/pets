package com.pets.goods.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.goods.domain.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
