package com.pets.member.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.member.adapter.out.MemberTimescardMapper;
import com.pets.member.adapter.out.TimescardTemplateMapper;
import com.pets.member.application.dto.MemberTimescardVO;
import com.pets.member.application.dto.TimescardTemplateSaveReq;
import com.pets.member.application.dto.TimescardTemplateVO;
import com.pets.member.domain.entity.MemberTimescard;
import com.pets.member.domain.entity.TimescardTemplate;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 次卡服务：模板管理、购卡计次、核销扣次。
 */
@Service
@RequiredArgsConstructor
public class TimesCardService {

    private final TimescardTemplateMapper templateMapper;
    private final MemberTimescardMapper memberTimescardMapper;
    private final IdService idService;

    // ==================== 模板 ====================

    public List<TimescardTemplateVO> listTemplate() {
        Long shopId = currentShopId();
        return templateMapper.selectList(
                new LambdaQueryWrapper<TimescardTemplate>().eq(TimescardTemplate::getShopId, shopId)
                        .orderByDesc(TimescardTemplate::getCreateTime)
        ).stream().map(t -> {
            TimescardTemplateVO vo = new TimescardTemplateVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setServiceId(t.getServiceId());
            vo.setTimes(t.getTimes());
            vo.setPrice(t.getPrice());
            vo.setValidDays(t.getValidDays());
            vo.setStatus(t.getStatus());
            vo.setCreateTime(t.getCreateTime());
            return vo;
        }).toList();
    }

    public void saveTemplate(TimescardTemplateSaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            TimescardTemplate t = new TimescardTemplate();
            t.setId(idService.next("timescard_template"));
            t.setShopId(shopId);
            t.setName(req.getName());
            t.setServiceId(req.getServiceId());
            t.setTimes(req.getTimes() != null ? req.getTimes() : 0);
            t.setPrice(req.getPrice());
            t.setValidDays(req.getValidDays() != null ? req.getValidDays() : 0);
            t.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            templateMapper.insert(t);
        } else {
            TimescardTemplate t = templateMapper.selectById(req.getId());
            if (t == null || !t.getShopId().equals(shopId)) throw new BizException("次卡模板不存在");
            t.setName(req.getName());
            t.setServiceId(req.getServiceId());
            t.setTimes(req.getTimes());
            t.setPrice(req.getPrice());
            t.setValidDays(req.getValidDays());
            t.setStatus(req.getStatus());
            templateMapper.updateById(t);
        }
    }

    // ==================== 购卡 & 核销 ====================

    @Transactional(rollbackFor = Exception.class)
    public void buy(Long memberId, Long templateId) {
        TimescardTemplate tpl = templateMapper.selectById(templateId);
        if (tpl == null) throw new BizException("次卡模板不存在");

        MemberTimescard mt = new MemberTimescard();
        mt.setId(idService.next("member_timescard"));
        mt.setMemberId(memberId);
        mt.setTemplateId(templateId);
        mt.setRemainTimes(tpl.getTimes());
        if (tpl.getValidDays() != null && tpl.getValidDays() > 0) {
            mt.setExpireTime(LocalDateTime.now().plusDays(tpl.getValidDays()));
        }
        memberTimescardMapper.insert(mt);
    }

    /**
     * 核销一次（服务结算时调用）。
     * 找该会员该模板下的有效次卡，扣减1次。
     */
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long memberId, Long templateId) {
        MemberTimescard mt = memberTimescardMapper.selectOne(
                new LambdaQueryWrapper<MemberTimescard>()
                        .eq(MemberTimescard::getMemberId, memberId)
                        .eq(MemberTimescard::getTemplateId, templateId)
                        .gt(MemberTimescard::getRemainTimes, 0)
                        .last("LIMIT 1 FOR UPDATE"));
        if (mt == null) throw new BizException("次卡次数不足");
        if (mt.getExpireTime() != null && mt.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException("次卡已过期");
        }
        mt.setRemainTimes(mt.getRemainTimes() - 1);
        int updated = memberTimescardMapper.updateById(mt);
        if (updated == 0) throw new BizException("核销并发冲突，请重试");
    }

    public List<MemberTimescardVO> listMemberTimescard(Long memberId) {
        return memberTimescardMapper.selectList(
                new LambdaQueryWrapper<MemberTimescard>().eq(MemberTimescard::getMemberId, memberId)
                        .orderByDesc(MemberTimescard::getCreateTime)
        ).stream().map(m -> {
            MemberTimescardVO vo = new MemberTimescardVO();
            vo.setId(m.getId());
            vo.setMemberId(m.getMemberId());
            vo.setTemplateId(m.getTemplateId());
            vo.setRemainTimes(m.getRemainTimes());
            vo.setExpireTime(m.getExpireTime());
            vo.setCreateTime(m.getCreateTime());
            TimescardTemplate tpl = templateMapper.selectById(m.getTemplateId());
            if (tpl != null) vo.setTemplateName(tpl.getName());
            return vo;
        }).toList();
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
