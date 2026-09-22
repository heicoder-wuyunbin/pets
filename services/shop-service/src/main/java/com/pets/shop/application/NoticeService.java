package com.pets.shop.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.exception.BizException;
import com.pets.shop.adapter.out.NoticeMapper;
import com.pets.shop.application.dto.NoticePageReq;
import com.pets.shop.application.dto.NoticeVO;
import com.pets.shop.domain.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 公告与行业资讯服务。
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Page<NoticeVO> page(NoticePageReq req) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .and(w -> w.eq(Notice::getShopId, 0).or().eq(Notice::getShopId, currentShopIdSafe()))
                .orderByDesc(Notice::getCreateTime);
        if (req.getType() != null) {
            wrapper.eq(Notice::getType, req.getType());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            wrapper.like(Notice::getTitle, req.getKeyword());
        }

        Page<Notice> page = noticeMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<NoticeVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<NoticeVO> records = page.getRecords().stream().map(this::toVO).toList();
        result.setRecords(records);
        return result;
    }

    public NoticeVO detail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BizException("公告不存在");
        }
        return toVO(notice);
    }

    private NoticeVO toVO(Notice n) {
        NoticeVO vo = new NoticeVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setCreateTime(n.getCreateTime() != null ? n.getCreateTime().format(DATE_FMT) : null);
        return vo;
    }

    private Long currentShopIdSafe() {
        var ctx = com.pets.common.context.AuthContextHolder.get();
        return ctx != null && ctx.getShopId() != null ? ctx.getShopId() : 0L;
    }
}
