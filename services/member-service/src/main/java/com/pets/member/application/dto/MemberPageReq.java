package com.pets.member.application.dto;

import lombok.Data;

@Data
public class MemberPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String keyword;
    private Integer memberLevel;
}
