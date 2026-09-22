package com.pets.trade;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.pets.trade", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.trade.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
@EnableFeignClients(basePackages = "com.pets.trade.adapter.out.client")
public class TradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
