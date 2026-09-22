package com.pets.stock.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.stock.adapter.out.StockInItemMapper;
import com.pets.stock.adapter.out.StockInMapper;
import com.pets.stock.adapter.out.StockMapper;
import com.pets.stock.application.dto.StockInSaveReq;
import com.pets.stock.application.dto.StockInVO;
import com.pets.stock.domain.entity.Stock;
import com.pets.stock.domain.entity.StockIn;
import com.pets.stock.domain.entity.StockInItem;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 入库单服务：创建入库单并同步库存。
 */
@Service
@RequiredArgsConstructor
public class StockInService {

    private final StockInMapper stockInMapper;
    private final StockInItemMapper stockInItemMapper;
    private final StockMapper stockMapper;
    private final StockService stockService;
    private final IdService idService;

    @Transactional(rollbackFor = Exception.class)
    public String create(StockInSaveReq req) {
        Long shopId = currentShopId();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BizException("入库明细不能为空");
        }

        String no = "IN" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", idService.next("stock_in_no") % 10000);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockInSaveReq.Item item : req.getItems()) {
            if (item.getQty() == null || item.getQty() <= 0) {
                throw new BizException("入库数量必须大于0");
            }
            totalAmount = totalAmount.add(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO
                    .multiply(BigDecimal.valueOf(item.getQty())));
        }

        StockIn stockIn = new StockIn();
        stockIn.setId(idService.next("stock_in"));
        stockIn.setShopId(shopId);
        stockIn.setNo(no);
        stockIn.setSupplier(req.getSupplier());
        stockIn.setTotalAmount(totalAmount);
        stockIn.setStatus(1);
        stockIn.setRemark(req.getRemark());
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        stockIn.setCreateUserId(ctx != null ? ctx.getAccountId() : null);
        stockInMapper.insert(stockIn);

        for (StockInSaveReq.Item item : req.getItems()) {
            StockInItem si = new StockInItem();
            si.setId(idService.next("stock_in_item"));
            si.setStockInId(stockIn.getId());
            si.setGoodsId(item.getGoodsId());
            si.setSkuId(item.getSkuId() != null ? item.getSkuId() : 0L);
            si.setQty(item.getQty());
            si.setPrice(item.getPrice());
            stockInItemMapper.insert(si);

            // 同步库存
            stockService.getOrCreate(shopId, item.getGoodsId(), item.getSkuId());
            Stock stock = stockMapper.selectOne(
                    new LambdaQueryWrapper<Stock>()
                            .eq(Stock::getShopId, shopId)
                            .eq(Stock::getGoodsId, item.getGoodsId())
                            .eq(Stock::getSkuId, item.getSkuId() != null ? item.getSkuId() : 0L)
                            .last("LIMIT 1"));
            stock.setQuantity(stock.getQuantity() + item.getQty());
            // 移动平均成本
            int newQty = stock.getQuantity();
            if (item.getPrice() != null && newQty > 0) {
                BigDecimal oldCost = stock.getAvgCost() != null ? stock.getAvgCost() : BigDecimal.ZERO;
                int oldQty = newQty - item.getQty();
                BigDecimal newCost = oldCost.multiply(BigDecimal.valueOf(oldQty))
                        .add(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())))
                        .divide(BigDecimal.valueOf(newQty), 2, java.math.RoundingMode.HALF_UP);
                stock.setAvgCost(newCost);
            }
            stockMapper.updateById(stock);
        }
        return no;
    }

    public Page<StockInVO> page(Integer pageNum, Integer pageSize, String keyword) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<StockIn>()
                .eq(StockIn::getShopId, shopId)
                .orderByDesc(StockIn::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(StockIn::getNo, keyword).or().like(StockIn::getSupplier, keyword));
        }
        Page<StockIn> page = stockInMapper.selectPage(
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20), wrapper);

        Page<StockInVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(si -> {
            StockInVO vo = new StockInVO();
            vo.setId(si.getId());
            vo.setNo(si.getNo());
            vo.setSupplier(si.getSupplier());
            vo.setTotalAmount(si.getTotalAmount());
            vo.setStatus(si.getStatus());
            vo.setRemark(si.getRemark());
            vo.setCreateTime(si.getCreateTime());
            // 明细
            List<StockInItem> items = stockInItemMapper.selectList(
                    new LambdaQueryWrapper<StockInItem>().eq(StockInItem::getStockInId, si.getId()));
            vo.setItems(items.stream().map(it -> {
                StockInVO.Item itVO = new StockInVO.Item();
                itVO.setGoodsId(it.getGoodsId());
                itVO.setSkuId(it.getSkuId());
                itVO.setQty(it.getQty());
                itVO.setPrice(it.getPrice());
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
