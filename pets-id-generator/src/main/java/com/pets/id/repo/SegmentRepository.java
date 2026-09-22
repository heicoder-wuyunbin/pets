package com.pets.id.repo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.id.entity.IdSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SegmentRepository extends BaseMapper<IdSegment> {

    @Update("UPDATE id_segment SET max_id = max_id + #{step}, version = version + 1, update_time = NOW() WHERE biz_type = #{bizType} AND version = #{version}")
    int updateMaxId(@Param("bizType") String bizType, @Param("step") int step, @Param("version") int version);
}
