package com.pets.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 * 标注在 Controller 方法上，由 LogAspect 自动采集并落库 operation_log。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoLog {

    /** 模块名称，如 "员工管理" */
    String module();

    /** 动作名称，如 "新增员工" */
    String action();
}
