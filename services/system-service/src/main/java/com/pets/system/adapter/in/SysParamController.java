package com.pets.system.adapter.in;

import com.pets.common.annotation.AutoLog;
import com.pets.common.web.R;
import com.pets.system.application.SysParamService;
import com.pets.system.application.dto.SysParamListReq;
import com.pets.system.application.dto.SysParamSaveReq;
import com.pets.system.application.dto.SysParamVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统参数配置接口。
 */
@RestController
@RequestMapping("/api/v1/sys-param")
@RequiredArgsConstructor
public class SysParamController {

    private final SysParamService sysParamService;

    @PostMapping("/list")
    public R<List<SysParamVO>> list(@RequestBody SysParamListReq req) {
        return R.ok(sysParamService.list(req));
    }

    @AutoLog(module = "系统参数", action = "保存参数")
    @PostMapping("/save")
    public R<Void> save(@Valid @RequestBody SysParamSaveReq req) {
        sysParamService.save(req);
        return R.ok();
    }
}
