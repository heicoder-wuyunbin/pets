package com.pets.stock.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.stock.adapter.out.StockOutItemMapper;
import com.pets.stock.adapter.out.StockOutMapper;
import com.pets.stock.application.dto.StockOutSaveReq;
import com.pets.stock.application.dto.StockOutVO;
import com.pets.stock.domain.entity.StockOut;
import com.pets.stock.domain.entity.StockOutItem;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 出库单服务：创建出库单并扣减库存。
 */
@Service
@RequiredArgsConstructor
public class StockOutService {

    private final StockOutMapper stockOutMapper;
    private final StockOutItemMapper stockOutItemMapper;
    private final StockService stockService;
    private final IdService idService;

    @Transactional(rollbackFor = Exception.class)
    public String create(StockOutSaveReq req) {
        Long shopId = currentShopId();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BizException("出库明细不能为空");
        }

        String no = "OUT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", idService.next("stock_out_no") % 10000);

        int totalQty = 0;
        for (StockOutSaveReq.Item item : req.getItems()) {
            if (item.getQty() == null || item.getQty() <= 0) {
                throw new BizException("出库数量必须大于0");
            }
            totalQty += item.getQty();
        }

        StockOut stockOut = new StockOut();
        stockOut.setId(idService.next("stock_out"));
        stockOut.setShopId(shopId);
        stockOut.setNo(no);
        stockOut.setType(req.getType() != null ? req.getType() : 1);
        stockOut.setTotalQty(totalQty);
        stockOut.setRemark(req.getRemark());
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        stockOut.setCreateUserId(ctx != null ? ctx.getAccountId() : null);
        stockOutMapper.insert(stockOut);

        for (StockOutSaveReq.Item item : req.getItems()) {
            StockOutItem soi = new StockOutItem();
            soi.setId(idService.next("stock_out_item"));
            soi.setStockOutId(stockOut.getId());
            soi.setGoodsId(item.getGoodsId());
            soi.setSkuId(item.getSkuId() != null ? item.getSkuId() : 0L);
            soi.setQty(item.getQty());
            stockOutItemMapper.insert(soi);

            // 扣减库存（乐观锁防超卖）
            stockService.deduct(shopId, item.getGoodsId(), item.getSkuId(), item.getQty());
        }
        return no;
    }

    public Page<StockOutVO> page(Integer pageNum, Integer pageSize, String keyword) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<StockOut>()
                .eq(StockOut::getShopId, shopId)
                .orderByDesc(StockOut::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(StockOut::getNo, keyword);
        }
        Page<StockOut> page = stockOutMapper.selectPage(
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20), wrapper);

        Page<StockOutVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(so -> {
            StockOutVO vo = new StockOutVO();
            vo.setId(so.getId());
            vo.setNo(so.getNo());
            vo.setType(so.getType());
            vo.setTotalQty(so.getTotalQty());
            vo.setRemark(so.getRemark());
            vo.setCreateTime(so.getCreateTime());
            List<StockOutItem> items = stockOutItemMapper.selectList(
                    new LambdaQueryWrapper<StockOutItem>().eq(StockOutItem::getStockOutId, so.getId()));
            vo.setItems(items.stream().map(it -> {
                StockOutVO.Item itVO = new StockOutVO.Item();
                itVO.setGoodsId(it.getGoodsId());
                itVO.setSkuId(it.getSkuId());
                itVO.setQty(it.getQty());
                return itVO;
            }).toList());
            return vo;
        }).toList());
        return result;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
