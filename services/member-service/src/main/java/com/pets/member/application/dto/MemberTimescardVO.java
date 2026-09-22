package com.pets.member.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberTimescardVO {
    private Long id;
    private Long memberId;
    private Long templateId;
    private String templateName;
    private Integer remainTimes;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
