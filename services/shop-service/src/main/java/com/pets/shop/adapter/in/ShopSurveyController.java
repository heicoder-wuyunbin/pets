package com.pets.shop.adapter.in;

import com.pets.common.web.R;
import com.pets.shop.application.SurveyService;
import com.pets.shop.application.dto.SurveyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺概况看板接口。
 */
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class ShopSurveyController {

    private final SurveyService surveyService;

    @PostMapping("/survey")
    public R<SurveyVO> survey() {
        return R.ok(surveyService.survey());
    }
}
