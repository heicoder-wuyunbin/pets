package com.pets.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工雇佣关系表（岗位维度）— 一人可在多集团/多店任职。
 */
@Data
@TableName("clerk")
public class Clerk {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long accountId;

    private Long shopId;

    private Long chainGroupId;

    private Long roleId;

    private Integer isSuper;

    private Integer status;

    private LocalDate hireDate;

    private LocalDate leaveDate;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
