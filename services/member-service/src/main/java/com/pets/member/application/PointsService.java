package com.pets.member.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pets.member.application.dto.PointsRuleReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 积分服务：规则配置（存 Redis）、按消费金额计算应得积分。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private static final String RULE_KEY = "member:points:rule";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final BigDecimal DEFAULT_RATIO = BigDecimal.ONE; // 默认1元=1积分
    private static final Integer DEFAULT_ENABLE = 1;
    private static final BigDecimal DEFAULT_OFFSET = BigDecimal.ZERO;

    public Map<String, Object> getRule() {
        try {
            String json = redisTemplate.opsForValue().get(RULE_KEY);
            if (json != null) {
                return objectMapper.readValue(json, Map.class);
            }
        } catch (Exception e) {
            log.warn("读取积分规则失败，使用默认值", e);
        }
        Map<String, Object> rule = new HashMap<>();
        rule.put("ratio", DEFAULT_RATIO);
        rule.put("enable", DEFAULT_ENABLE);
        rule.put("offset", DEFAULT_OFFSET);
        return rule;
    }

    public void saveRule(PointsRuleReq req) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("ratio", req.getRatio() != null ? req.getRatio() : DEFAULT_RATIO);
        rule.put("enable", req.getEnable() != null ? req.getEnable() : DEFAULT_ENABLE);
        rule.put("offset", req.getOffset() != null ? req.getOffset() : DEFAULT_OFFSET);
        try {
            redisTemplate.opsForValue().set(RULE_KEY, objectMapper.writeValueAsString(rule));
        } catch (Exception e) {
            throw new RuntimeException("保存积分规则失败", e);
        }
    }

    /**
     * 根据消费金额计算应得积分。
     *
     * @param amount 消费金额
     * @return 应得积分（0 表示未启用或未达到门槛）
     */
    public int calcPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return 0;
        Map<String, Object> rule = getRule();
        int enable = toInt(rule.get("enable"), DEFAULT_ENABLE);
        if (enable != 1) return 0;

        BigDecimal offset = toBigDecimal(rule.get("offset"), DEFAULT_OFFSET);
        if (amount.compareTo(offset) < 0) return 0;

        BigDecimal ratio = toBigDecimal(rule.get("ratio"), DEFAULT_RATIO);
        // 积分 = (金额 - 门槛) * 比率，向下取整
        return amount.subtract(offset).multiply(ratio).setScale(0, RoundingMode.DOWN).intValue();
    }

    private int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return def; }
    }

    private BigDecimal toBigDecimal(Object o, BigDecimal def) {
        if (o == null) return def;
        if (o instanceof Number) return new BigDecimal(o.toString());
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return def; }
    }
}
