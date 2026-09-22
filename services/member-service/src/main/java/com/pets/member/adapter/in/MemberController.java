package com.pets.member.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.member.application.MemberService;
import com.pets.member.application.dto.MemberDetailVO;
import com.pets.member.application.dto.MemberListVO;
import com.pets.member.application.dto.MemberPageReq;
import com.pets.member.application.dto.MemberSaveReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/page")
    public R<List<MemberListVO>> page(@RequestBody MemberPageReq req) {
        Page<MemberListVO> page = memberService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/detail")
    public R<MemberDetailVO> detail(@RequestBody Map<String, Long> body) {
        return R.ok(memberService.detail(body.get("id")));
    }

    @AutoLog(module = "会员档案", action = "保存会员")
    @PostMapping("/save")
    public R<Void> save(@Valid @RequestBody MemberSaveReq req) {
        memberService.save(req);
        return R.ok();
    }

    @AutoLog(module = "会员档案", action = "删除会员")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Long> body) {
        memberService.delete(body.get("id"));
        return R.ok();
    }
}
