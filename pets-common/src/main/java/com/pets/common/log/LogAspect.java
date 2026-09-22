package com.pets.common.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pets.common.annotation.AutoLog;
import com.pets.common.context.AuthContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 操作日志切面。
 * 拦截标注 @AutoLog 的方法，采集操作人/模块/动作/参数/IP，
 * 通过 Spring 事件机制发布 OperationLogEvent，由具体服务落库。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 需要脱敏的字段名 */
    private static final Pattern SENSITIVE_KEYS = Pattern.compile(
            "(?i)(password|pwd|code|secret|token|phone|mobile)");

    @Around("@annotation(com.pets.common.annotation.AutoLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AutoLog autoLog = method.getAnnotation(AutoLog.class);

        String module = autoLog.module();
        String action = autoLog.action();
        String params = buildParams(joinPoint);
        String ip = currentIp();

        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        Long shopId = ctx != null ? ctx.getShopId() : null;
        Long userId = ctx != null ? ctx.getAccountId() : null;

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            publish(shopId, userId, module, action, params, ip, 1, null);
            return result;
        } catch (Throwable e) {
            publish(shopId, userId, module, action, params, ip, 0, e.getMessage());
            throw e;
        } finally {
            log.debug("[AutoLog] {} - {} 耗时 {}ms", module, action, System.currentTimeMillis() - start);
        }
    }

    private void publish(Long shopId, Long userId, String module, String action,
                         String params, String ip, Integer status, String failReason) {
        try {
            OperationLogEvent event = new OperationLogEvent();
            event.setShopId(shopId);
            event.setUserId(userId);
            event.setModule(module);
            event.setAction(action);
            event.setParams(params);
            event.setIp(ip);
            event.setStatus(status);
            event.setFailReason(failReason);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.warn("发布操作日志事件失败", e);
        }
    }

    private String buildParams(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            // 简单脱敏：将参数序列化为 JSON 后对敏感字段打码
            String json = objectMapper.writeValueAsString(Arrays.asList(args));
            return maskSensitive(json);
        } catch (JsonProcessingException e) {
            return "[参数序列化失败]";
        }
    }

    /**
     * 对 JSON 字符串中的敏感字段值进行打码处理。
     * 匹配 "key":"value" 中 key 命中敏感词的 value。
     */
    private String maskSensitive(String json) {
        if (json == null) {
            return null;
        }
        // 匹配 "sensitiveKey":"value" 形式
        return json.replaceAll(
                "(\"(?i)(?:password|pwd|code|secret|token|phone|mobile)\")\\s*:\\s*\"([^\"]*)\"",
                "$1:\"******\"");
    }

    private String currentIp() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        HttpServletRequest request = sra.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return (ip != null && !ip.isBlank()) ? ip : request.getRemoteAddr();
    }
}
