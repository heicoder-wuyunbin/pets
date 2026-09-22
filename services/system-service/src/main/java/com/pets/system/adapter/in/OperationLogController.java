package com.pets.system.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.system.application.OperationLogService;
import com.pets.system.application.dto.OperationLogPageReq;
import com.pets.system.application.dto.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志查询接口。
 */
@RestController
@RequestMapping("/api/v1/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @PostMapping("/page")
    public R<List<OperationLogVO>> page(@RequestBody OperationLogPageReq req) {
        Page<OperationLogVO> page = operationLogService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }
}
