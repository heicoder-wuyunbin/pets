package com.pets.member.adapter.out;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pets.member.domain.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
