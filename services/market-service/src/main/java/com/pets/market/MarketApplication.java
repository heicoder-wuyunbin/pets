package com.pets.market;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pets.market", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.market.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
public class MarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
    }
}
