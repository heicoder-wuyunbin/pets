package com.pets.goods;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pets.goods", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.goods.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
public class GoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }
}
