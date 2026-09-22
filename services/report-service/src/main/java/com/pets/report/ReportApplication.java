package com.pets.report;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.pets.report", "com.pets.common"})
@MapperScan(basePackages = {"com.pets.report.adapter.out", "com.pets.id.repo"}, annotationClass = Mapper.class)
public class ReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
