package com.pets.system.application.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工 VO。
 */
@Data
public class ClerkVO {
    private Long id;
    private Long accountId;
    private String phone;
    private String truename;
    private Long roleId;
    private String roleName;
    private Integer isSuper;
    private Integer status;
    private LocalDate hireDate;
}
