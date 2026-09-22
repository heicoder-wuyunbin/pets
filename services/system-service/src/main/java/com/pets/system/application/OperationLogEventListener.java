package com.pets.system.application;

import com.pets.common.log.OperationLogEvent;
import com.pets.id.service.IdService;
import com.pets.system.adapter.out.OperationLogMapper;
import com.pets.system.domain.entity.OperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志事件监听器：接收 LogAspect 发布的事件并落库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogEventListener {

    private final OperationLogMapper operationLogMapper;
    private final IdService idService;

    @Async
    @EventListener
    public void onOperationLog(OperationLogEvent event) {
        try {
            OperationLog entity = new OperationLog();
            entity.setId(idService.next("operation_log"));
            entity.setShopId(event.getShopId());
            entity.setUserId(event.getUserId());
            entity.setUserName(event.getUserName());
            entity.setModule(event.getModule());
            entity.setAction(event.getAction());
            entity.setParams(event.getParams());
            entity.setIp(event.getIp());
            entity.setStatus(event.getStatus());
            entity.setFailReason(event.getFailReason());
            entity.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("操作日志落库失败", e);
        }
    }
}
