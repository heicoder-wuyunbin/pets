package com.pets.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.system.adapter.out.OperationLogMapper;
import com.pets.system.application.dto.OperationLogPageReq;
import com.pets.system.application.dto.OperationLogVO;
import com.pets.system.domain.entity.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志服务：分页查询。
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Page<OperationLogVO> page(OperationLogPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getShopId, shopId)
                .orderByDesc(OperationLog::getCreateTime);

        if (req.getModule() != null && !req.getModule().isBlank()) {
            wrapper.eq(OperationLog::getModule, req.getModule());
        }
        if (req.getUserName() != null && !req.getUserName().isBlank()) {
            wrapper.like(OperationLog::getUserName, req.getUserName());
        }
        if (req.getStartTime() != null && !req.getStartTime().isBlank()) {
            wrapper.ge(OperationLog::getCreateTime,
                    LocalDate.parse(req.getStartTime(), DATE_FMT).atStartOfDay());
        }
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            wrapper.le(OperationLog::getCreateTime,
                    LocalDate.parse(req.getEndTime(), DATE_FMT).atTime(23, 59, 59));
        }

        Page<OperationLog> page = operationLogMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<OperationLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private OperationLogVO toVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setShopId(log.getShopId());
        vo.setUserId(log.getUserId());
        vo.setUserName(log.getUserName());
        vo.setModule(log.getModule());
        vo.setAction(log.getAction());
        vo.setParams(log.getParams());
        vo.setIp(log.getIp());
        vo.setStatus(log.getStatus());
        vo.setFailReason(log.getFailReason());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
