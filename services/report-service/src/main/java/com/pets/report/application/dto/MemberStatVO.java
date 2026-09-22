package com.pets.report.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberStatVO {
    private long totalMember;
    private long newMemberToday;
    private long activeMemberToday;
    private List<LevelDist> levelDist;

    @Data
    public static class LevelDist {
        private Integer level;
        private Long count;
    }
}
