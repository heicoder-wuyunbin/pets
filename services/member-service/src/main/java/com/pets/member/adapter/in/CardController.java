package com.pets.member.adapter.in;

import com.pets.common.annotation.AutoLog;
import com.pets.common.web.R;
import com.pets.member.application.CardService;
import com.pets.member.application.dto.CardTypeSaveReq;
import com.pets.member.application.dto.CardTypeVO;
import com.pets.member.application.dto.MemberCardVO;
import com.pets.member.application.dto.RechargeReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/member/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/type/list")
    public R<List<CardTypeVO>> listType() {
        return R.ok(cardService.listCardType());
    }

    @AutoLog(module = "会员卡", action = "保存卡种")
    @PostMapping("/type/save")
    public R<Void> saveType(@Valid @RequestBody CardTypeSaveReq req) {
        cardService.saveCardType(req);
        return R.ok();
    }

    @AutoLog(module = "会员卡", action = "开卡充值")
    @PostMapping("/recharge")
    public R<Void> recharge(@Valid @RequestBody RechargeReq req) {
        cardService.recharge(req);
        return R.ok();
    }

    @PostMapping("/list")
    public R<List<MemberCardVO>> list(@RequestBody Map<String, Long> body) {
        return R.ok(cardService.listMemberCard(body.get("memberId")));
    }
}
