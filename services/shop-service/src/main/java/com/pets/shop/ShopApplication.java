package com.pets.shop;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pets.shop", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.shop.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
