package com.pets.member.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.member.adapter.out.CardTypeMapper;
import com.pets.member.adapter.out.MemberAccountLogMapper;
import com.pets.member.adapter.out.MemberAccountMapper;
import com.pets.member.adapter.out.MemberCardMapper;
import com.pets.member.application.dto.*;
import com.pets.member.domain.entity.CardType;
import com.pets.member.domain.entity.MemberAccount;
import com.pets.member.domain.entity.MemberAccountLog;
import com.pets.member.domain.entity.MemberCard;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员卡服务：卡种管理、开卡充值、会员卡列表。
 * 充值金额入 member_account.balance，并记流水。
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardTypeMapper cardTypeMapper;
    private final MemberCardMapper memberCardMapper;
    private final MemberAccountMapper accountMapper;
    private final MemberAccountLogMapper logMapper;
    private final IdService idService;

    // ==================== 卡种 ====================

    public List<CardTypeVO> listCardType() {
        Long shopId = currentShopId();
        return cardTypeMapper.selectList(
                new LambdaQueryWrapper<CardType>().eq(CardType::getShopId, shopId)
                        .orderByDesc(CardType::getCreateTime)
        ).stream().map(c -> {
            CardTypeVO vo = new CardTypeVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setFaceValue(c.getFaceValue());
            vo.setDiscount(c.getDiscount());
            vo.setValidDays(c.getValidDays());
            vo.setStatus(c.getStatus());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).toList();
    }

    public void saveCardType(CardTypeSaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            CardType ct = new CardType();
            ct.setId(idService.next("card_type"));
            ct.setShopId(shopId);
            ct.setName(req.getName());
            ct.setFaceValue(req.getFaceValue() != null ? req.getFaceValue() : BigDecimal.ZERO);
            ct.setDiscount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ONE);
            ct.setValidDays(req.getValidDays() != null ? req.getValidDays() : 0);
            ct.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            cardTypeMapper.insert(ct);
        } else {
            CardType ct = cardTypeMapper.selectById(req.getId());
            if (ct == null || !ct.getShopId().equals(shopId)) throw new BizException("卡种不存在");
            ct.setName(req.getName());
            ct.setFaceValue(req.getFaceValue());
            ct.setDiscount(req.getDiscount());
            ct.setValidDays(req.getValidDays());
            ct.setStatus(req.getStatus());
            cardTypeMapper.updateById(ct);
        }
    }

    // ==================== 开卡充值 ====================

    @Transactional(rollbackFor = Exception.class)
    public void recharge(RechargeReq req) {
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("充值金额必须大于0");
        }
        Long shopId = currentShopId();

        MemberAccount acc = accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, req.getMemberId()));
        if (acc == null) throw new BizException("会员账户不存在");

        // 余额增加
        acc.setBalance(acc.getBalance().add(req.getAmount()));
        acc.setTotalRecharge(acc.getTotalRecharge().add(req.getAmount()));
        int updated = accountMapper.updateById(acc);
        if (updated == 0) throw new BizException("充值并发冲突，请重试");

        // 创建会员卡记录
        MemberCard card = new MemberCard();
        card.setId(idService.next("member_card"));
        card.setMemberId(req.getMemberId());
        card.setCardTypeId(req.getCardTypeId() != null ? req.getCardTypeId() : 0L);
        card.setBalance(req.getAmount());
        if (req.getCardTypeId() != null) {
            CardType ct = cardTypeMapper.selectById(req.getCardTypeId());
            if (ct != null && ct.getValidDays() != null && ct.getValidDays() > 0) {
                card.setExpireTime(LocalDateTime.now().plusDays(ct.getValidDays()));
            }
        }
        memberCardMapper.insert(card);

        // 记流水
        saveLog(req.getMemberId(), 1, req.getAmount(), acc.getBalance(), 0, acc.getPoints(), null, "充值");
    }

    public List<MemberCardVO> listMemberCard(Long memberId) {
        return memberCardMapper.selectList(
                new LambdaQueryWrapper<MemberCard>().eq(MemberCard::getMemberId, memberId)
                        .orderByDesc(MemberCard::getCreateTime)
        ).stream().map(c -> {
            MemberCardVO vo = new MemberCardVO();
            vo.setId(c.getId());
            vo.setMemberId(c.getMemberId());
            vo.setCardTypeId(c.getCardTypeId());
            vo.setBalance(c.getBalance());
            vo.setExpireTime(c.getExpireTime());
            vo.setCreateTime(c.getCreateTime());
            if (c.getCardTypeId() != null && c.getCardTypeId() > 0) {
                CardType ct = cardTypeMapper.selectById(c.getCardTypeId());
                if (ct != null) vo.setCardTypeName(ct.getName());
            }
            return vo;
        }).toList();
    }

    private void saveLog(Long memberId, int type, BigDecimal amount, BigDecimal balanceAfter,
                         int points, int pointsAfter, Long orderId, String remark) {
        MemberAccountLog log = new MemberAccountLog();
        log.setId(idService.next("member_account_log"));
        log.setMemberId(memberId);
        log.setType(type);
        log.setAmount(amount);
        log.setBalanceAfter(balanceAfter);
        log.setPoints(points);
        log.setPointsAfter(pointsAfter);
        log.setOrderId(orderId);
        log.setRemark(remark);
        logMapper.insert(log);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
