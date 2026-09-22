package com.pets.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.id.service.IdService;
import com.pets.system.adapter.out.SysParamMapper;
import com.pets.system.application.dto.SysParamListReq;
import com.pets.system.application.dto.SysParamSaveReq;
import com.pets.system.application.dto.SysParamVO;
import com.pets.system.domain.entity.SysParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统参数配置服务：列表/保存 + Redis 缓存失效。
 */
@Service
@RequiredArgsConstructor
public class SysParamService {

    private final SysParamMapper sysParamMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final IdService idService;

    public List<SysParamVO> list(SysParamListReq req) {
        Long shopId = currentShopId();
        // 查询当前租户参数 + 全局默认参数（shop_id=0）
        LambdaQueryWrapper<SysParam> wrapper = new LambdaQueryWrapper<SysParam>()
                .in(SysParam::getShopId, shopId, 0L);
        if (req.getGroup() != null && !req.getGroup().isBlank()) {
            wrapper.eq(SysParam::getGroupKey, req.getGroup());
        }
        wrapper.orderByAsc(SysParam::getGroupKey).orderByAsc(SysParam::getParamKey);

        List<SysParam> list = sysParamMapper.selectList(wrapper);
        // 租户参数优先于全局默认
        return list.stream()
                .filter(p -> p.getShopId().equals(shopId)
                        || list.stream().noneMatch(q -> q.getShopId().equals(shopId)
                        && q.getParamKey().equals(p.getParamKey())))
                .map(this::toVO)
                .toList();
    }

    public void save(SysParamSaveReq req) {
        Long shopId = currentShopId();
        for (SysParamSaveReq.ParamItem item : req.getItems()) {
            SysParam existing = sysParamMapper.selectOne(
                    new LambdaQueryWrapper<SysParam>()
                            .eq(SysParam::getShopId, shopId)
                            .eq(SysParam::getParamKey, item.getParamKey()));
            if (existing != null) {
                existing.setParamValue(item.getParamValue());
                sysParamMapper.updateById(existing);
            } else {
                // 从全局默认复制 groupKey/remark，或留空
                SysParam global = sysParamMapper.selectOne(
                        new LambdaQueryWrapper<SysParam>()
                                .eq(SysParam::getShopId, 0L)
                                .eq(SysParam::getParamKey, item.getParamKey()));
                SysParam param = new SysParam();
                param.setId(idService.next("sys_param"));
                param.setShopId(shopId);
                param.setParamKey(item.getParamKey());
                param.setParamValue(item.getParamValue());
                if (global != null) {
                    param.setGroupKey(global.getGroupKey());
                    param.setRemark(global.getRemark());
                }
                sysParamMapper.insert(param);
            }
            // 失效缓存
            stringRedisTemplate.delete("param:" + shopId + ":" + item.getParamKey());
        }
    }

    private SysParamVO toVO(SysParam p) {
        SysParamVO vo = new SysParamVO();
        vo.setId(p.getId());
        vo.setGroupKey(p.getGroupKey());
        vo.setParamKey(p.getParamKey());
        vo.setParamValue(p.getParamValue());
        vo.setRemark(p.getRemark());
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
