package com.pets.member.adapter.in;

import com.pets.common.annotation.AutoLog;
import com.pets.common.web.R;
import com.pets.member.application.TimesCardService;
import com.pets.member.application.dto.MemberTimescardVO;
import com.pets.member.application.dto.TimescardTemplateSaveReq;
import com.pets.member.application.dto.TimescardTemplateVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/member/timescard")
@RequiredArgsConstructor
public class TimesCardController {

    private final TimesCardService timesCardService;

    @PostMapping("/template/list")
    public R<List<TimescardTemplateVO>> listTemplate() {
        return R.ok(timesCardService.listTemplate());
    }

    @AutoLog(module = "次卡", action = "保存模板")
    @PostMapping("/template/save")
    public R<Void> saveTemplate(@Valid @RequestBody TimescardTemplateSaveReq req) {
        timesCardService.saveTemplate(req);
        return R.ok();
    }

    @AutoLog(module = "次卡", action = "购卡")
    @PostMapping("/buy")
    public R<Void> buy(@RequestBody Map<String, Long> body) {
        timesCardService.buy(body.get("memberId"), body.get("templateId"));
        return R.ok();
    }

    /** 核销一次（服务结算调用） */
    @PostMapping("/consume")
    public R<Void> consume(@RequestBody Map<String, Long> body) {
        timesCardService.consume(body.get("memberId"), body.get("templateId"));
        return R.ok();
    }

    @PostMapping("/list")
    public R<List<MemberTimescardVO>> list(@RequestBody Map<String, Long> body) {
        return R.ok(timesCardService.listMemberTimescard(body.get("memberId")));
    }
}
