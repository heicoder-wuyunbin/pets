package com.pets.id.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("id_segment")
public class IdSegment {

    @TableId
    private Long id;

    private String bizType;

    private Long maxId;

    private Integer step;

    @Version
    private Integer version;

    private LocalDateTime updateTime;
}
