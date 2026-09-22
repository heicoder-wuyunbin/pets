package com.pets.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.system.adapter.out.SysParamMapper;
import com.pets.system.domain.entity.SysParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 系统参数读取工具：带 Redis 缓存，未设置时返回默认值。
 * 供本服务内业务使用。
 */
@Component
@RequiredArgsConstructor
public class ParamReader {

    private final SysParamMapper sysParamMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 读取参数值，未配置时返回 defaultValue。
     */
    public String get(Long shopId, String key, String defaultValue) {
        String cacheKey = "param:" + shopId + ":" + key;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        // 查租户级
        SysParam param = sysParamMapper.selectOne(
                new LambdaQueryWrapper<SysParam>()
                        .eq(SysParam::getShopId, shopId)
                        .eq(SysParam::getParamKey, key));
        if (param == null) {
            // 查全局默认
            param = sysParamMapper.selectOne(
                    new LambdaQueryWrapper<SysParam>()
                            .eq(SysParam::getShopId, 0L)
                            .eq(SysParam::getParamKey, key));
        }
        String value = param != null ? param.getParamValue() : defaultValue;
        if (value != null) {
            stringRedisTemplate.opsForValue().set(cacheKey, value, Duration.ofMinutes(30));
        }
        return value;
    }
}
