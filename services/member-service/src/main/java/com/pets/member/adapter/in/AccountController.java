package com.pets.member.adapter.in;

import com.pets.common.web.R;
import com.pets.member.application.AccountService;
import com.pets.member.application.dto.AccountLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账户接口：余额扣费/退款、积分累加/抵现、流水查询。
 * 供收银（trade-service）调用。
 */
@RestController
@RequestMapping("/api/v1/member/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /** 余额扣费（收银） */
    @PostMapping("/deduct")
    public R<Void> deduct(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        accountService.deductBalance(memberId, amount, orderId);
        return R.ok();
    }

    /** 退款回补（收银退款） */
    @PostMapping("/refund")
    public R<Void> refund(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        accountService.refundBalance(memberId, amount, orderId);
        return R.ok();
    }

    /** 累加积分（收银支付后） */
    @PostMapping("/points/add")
    public R<Void> addPoints(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        int points = Integer.parseInt(body.get("points").toString());
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        accountService.addPoints(memberId, points, orderId);
        return R.ok();
    }

    /** 积分抵现（收银） */
    @PostMapping("/points/deduct")
    public R<Void> deductPoints(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        int points = Integer.parseInt(body.get("points").toString());
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        accountService.deductPoints(memberId, points, orderId);
        return R.ok();
    }

    /** 账户流水 */
    @PostMapping("/logs")
    public R<List<AccountLogVO>> logs(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(body.get("memberId").toString());
        Integer type = body.get("type") != null ? Integer.parseInt(body.get("type").toString()) : null;
        return R.ok(accountService.listLogs(memberId, type));
    }
}
