package com.pets.auth;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * auth-service 启动类。
 * {@code @MapperScan} 仅扫描标注 {@link Mapper} 的接口，
 * 避免 MyBatis 把同包下的 MongoDB Repository 也当作 Mapper 代理。
 */
@SpringBootApplication(scanBasePackages = {"com.pets.auth", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.auth.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
