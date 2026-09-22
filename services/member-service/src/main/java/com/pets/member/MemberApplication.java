package com.pets.member;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pets.member", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.member.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
