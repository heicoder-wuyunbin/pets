package com.pets.shop.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.shop.application.NoticeService;
import com.pets.shop.application.dto.NoticePageReq;
import com.pets.shop.application.dto.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公告与行业资讯接口。
 */
@RestController
@RequestMapping("/api/v1/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/page")
    public R<java.util.List<NoticeVO>> page(@RequestBody NoticePageReq req) {
        Page<NoticeVO> page = noticeService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/detail")
    public R<NoticeVO> detail(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        return R.ok(noticeService.detail(id));
    }
}
