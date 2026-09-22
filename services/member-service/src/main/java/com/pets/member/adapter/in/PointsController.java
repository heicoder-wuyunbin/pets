package com.pets.member.adapter.in;

import com.pets.common.web.R;
import com.pets.member.application.PointsService;
import com.pets.member.application.dto.PointsRuleReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/member/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @PostMapping("/rule/get")
    public R<Map<String, Object>> getRule() {
        return R.ok(pointsService.getRule());
    }

    @PostMapping("/rule/save")
    public R<Void> saveRule(@RequestBody PointsRuleReq req) {
        pointsService.saveRule(req);
        return R.ok();
    }
}
