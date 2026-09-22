package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemberDetailVO {
    private Long id;
    private String mobile;
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private Integer memberLevel;
    private String tags;
    private String remark;
    private LocalDateTime createTime;

    private BigDecimal balance;
    private Integer points;
    private BigDecimal totalConsume;
    private BigDecimal totalRecharge;

    private List<PetVO> pets;

    @Data
    public static class PetVO {
        private Long id;
        private String petName;
        private String petType;
        private String breed;
        private Integer gender;
        private BigDecimal weight;
        private LocalDate birthday;
        private String vaccine;
    }
}
