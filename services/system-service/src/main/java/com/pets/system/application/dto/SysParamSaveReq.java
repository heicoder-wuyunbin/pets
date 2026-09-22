package com.pets.system.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 系统参数保存请求。
 */
@Data
public class SysParamSaveReq {

    @NotEmpty(message = "参数项不能为空")
    private List<ParamItem> items;

    @Data
    public static class ParamItem {
        private String paramKey;
        private String paramValue;
    }
}
