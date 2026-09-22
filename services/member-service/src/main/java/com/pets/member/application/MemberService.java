package com.pets.member.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.member.adapter.out.MemberAccountMapper;
import com.pets.member.adapter.out.MemberMapper;
import com.pets.member.adapter.out.MemberPetMapper;
import com.pets.member.application.dto.*;
import com.pets.member.domain.entity.Member;
import com.pets.member.domain.entity.MemberAccount;
import com.pets.member.domain.entity.MemberPet;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberPetMapper petMapper;
    private final MemberAccountMapper accountMapper;
    private final IdService idService;

    public Page<MemberListVO> page(MemberPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<Member>()
                .eq(Member::getShopId, shopId)
                .orderByDesc(Member::getCreateTime);

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Member::getName, req.getKeyword())
                    .or().like(Member::getMobile, req.getKeyword()));
        }
        if (req.getMemberLevel() != null) {
            wrapper.eq(Member::getMemberLevel, req.getMemberLevel());
        }

        Page<Member> page = memberMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<MemberListVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(m -> {
            MemberListVO vo = new MemberListVO();
            vo.setId(m.getId());
            vo.setMobile(m.getMobile());
            vo.setName(m.getName());
            vo.setGender(m.getGender());
            vo.setMemberLevel(m.getMemberLevel());
            vo.setTags(m.getTags());
            vo.setCreateTime(m.getCreateTime());
            MemberAccount acc = accountMapper.selectOne(
                    new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, m.getId()));
            if (acc != null) {
                vo.setBalance(acc.getBalance());
                vo.setPoints(acc.getPoints());
                vo.setTotalConsume(acc.getTotalConsume());
            }
            return vo;
        }).toList());
        return result;
    }

    public MemberDetailVO detail(Long id) {
        Member member = getOwnedMember(id);
        MemberDetailVO vo = new MemberDetailVO();
        vo.setId(member.getId());
        vo.setMobile(member.getMobile());
        vo.setName(member.getName());
        vo.setGender(member.getGender());
        vo.setBirthday(member.getBirthday());
        vo.setMemberLevel(member.getMemberLevel());
        vo.setTags(member.getTags());
        vo.setRemark(member.getRemark());
        vo.setCreateTime(member.getCreateTime());

        MemberAccount acc = accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, id));
        if (acc != null) {
            vo.setBalance(acc.getBalance());
            vo.setPoints(acc.getPoints());
            vo.setTotalConsume(acc.getTotalConsume());
            vo.setTotalRecharge(acc.getTotalRecharge());
        }

        List<MemberPet> pets = petMapper.selectList(
                new LambdaQueryWrapper<MemberPet>().eq(MemberPet::getMemberId, id));
        vo.setPets(pets.stream().map(p -> {
            MemberDetailVO.PetVO pvo = new MemberDetailVO.PetVO();
            pvo.setId(p.getId());
            pvo.setPetName(p.getPetName());
            pvo.setPetType(p.getPetType());
            pvo.setBreed(p.getBreed());
            pvo.setGender(p.getGender());
            pvo.setWeight(p.getWeight());
            pvo.setBirthday(p.getBirthday());
            pvo.setVaccine(p.getVaccine());
            return pvo;
        }).toList());

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(MemberSaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            // 手机号唯一校验
            Long exist = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getShopId, shopId)
                            .eq(Member::getMobile, req.getMobile()));
            if (exist > 0) {
                throw new BizException("该手机号已存在会员");
            }
            Member member = new Member();
            member.setId(idService.next("member"));
            member.setShopId(shopId);
            member.setMobile(req.getMobile());
            member.setName(req.getName());
            member.setGender(req.getGender() != null ? req.getGender() : 0);
            member.setBirthday(req.getBirthday());
            member.setMemberLevel(1);
            member.setTags(req.getTags());
            member.setRemark(req.getRemark());
            memberMapper.insert(member);

            // 初始化账户
            MemberAccount acc = new MemberAccount();
            acc.setId(idService.next("member_account"));
            acc.setMemberId(member.getId());
            acc.setBalance(BigDecimal.ZERO);
            acc.setPoints(0);
            acc.setTotalConsume(BigDecimal.ZERO);
            acc.setTotalRecharge(BigDecimal.ZERO);
            accountMapper.insert(acc);

            // 宠物
            savePets(member.getId(), req.getPets());
        } else {
            Member member = getOwnedMember(req.getId());
            member.setName(req.getName());
            member.setGender(req.getGender());
            member.setBirthday(req.getBirthday());
            member.setTags(req.getTags());
            member.setRemark(req.getRemark());
            memberMapper.updateById(member);

            // 重建宠物
            petMapper.delete(new LambdaQueryWrapper<MemberPet>().eq(MemberPet::getMemberId, member.getId()));
            savePets(member.getId(), req.getPets());
        }
    }

    private void savePets(Long memberId, List<MemberSaveReq.PetReq> pets) {
        if (pets == null || pets.isEmpty()) return;
        for (MemberSaveReq.PetReq p : pets) {
            MemberPet pet = new MemberPet();
            pet.setId(idService.next("member_pet"));
            pet.setMemberId(memberId);
            pet.setPetName(p.getPetName());
            pet.setPetType(p.getPetType());
            pet.setBreed(p.getBreed());
            pet.setGender(p.getGender() != null ? p.getGender() : 0);
            pet.setWeight(p.getWeight());
            pet.setBirthday(p.getBirthday());
            pet.setVaccine(p.getVaccine());
            petMapper.insert(pet);
        }
    }

    public void delete(Long id) {
        Member member = getOwnedMember(id);
        memberMapper.deleteById(id);
        petMapper.delete(new LambdaQueryWrapper<MemberPet>().eq(MemberPet::getMemberId, id));
        accountMapper.delete(new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, id));
    }

    private Member getOwnedMember(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) throw new BizException("会员不存在");
        if (!member.getShopId().equals(currentShopId())) throw new BizException("无权操作该会员");
        return member;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
