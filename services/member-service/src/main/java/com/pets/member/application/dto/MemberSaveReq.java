package com.pets.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MemberSaveReq {
    private Long id;

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender;

    private LocalDate birthday;

    private String tags;

    private String remark;

    private List<PetReq> pets;

    @Data
    public static class PetReq {
        private Long id;
        private String petName;
        private String petType;
        private String breed;
        private Integer gender;
        private java.math.BigDecimal weight;
        private LocalDate birthday;
        private String vaccine;
    }
}
