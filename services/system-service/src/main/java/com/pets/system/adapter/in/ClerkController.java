package com.pets.system.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.system.application.ClerkService;
import com.pets.system.application.dto.ClerkAddReq;
import com.pets.system.application.dto.ClerkEditReq;
import com.pets.system.application.dto.ClerkPageReq;
import com.pets.system.application.dto.ClerkResetPwdReq;
import com.pets.system.application.dto.ClerkStatusReq;
import com.pets.system.application.dto.ClerkVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工管理接口。
 */
@RestController
@RequestMapping("/api/v1/clerk")
@RequiredArgsConstructor
public class ClerkController {

    private final ClerkService clerkService;

    @PostMapping("/page")
    public R<List<ClerkVO>> page(@RequestBody ClerkPageReq req) {
        Page<ClerkVO> page = clerkService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @AutoLog(module = "员工管理", action = "新增员工")
    @PostMapping("/add")
    public R<Void> add(@Valid @RequestBody ClerkAddReq req) {
        clerkService.add(req);
        return R.ok();
    }

    @AutoLog(module = "员工管理", action = "编辑员工")
    @PostMapping("/edit")
    public R<Void> edit(@Valid @RequestBody ClerkEditReq req) {
        clerkService.edit(req);
        return R.ok();
    }

    @AutoLog(module = "员工管理", action = "切换员工状态")
    @PostMapping("/status")
    public R<Void> status(@Valid @RequestBody ClerkStatusReq req) {
        clerkService.status(req);
        return R.ok();
    }

    @AutoLog(module = "员工管理", action = "重置密码")
    @PostMapping("/reset-pwd")
    public R<Void> resetPwd(@Valid @RequestBody ClerkResetPwdReq req) {
        clerkService.resetPwd(req);
        return R.ok();
    }
}
