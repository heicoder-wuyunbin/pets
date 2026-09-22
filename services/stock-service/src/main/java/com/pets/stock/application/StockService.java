package com.pets.stock.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.stock.adapter.out.StockMapper;
import com.pets.stock.application.dto.StockPageReq;
import com.pets.stock.application.dto.StockVO;
import com.pets.stock.domain.entity.Stock;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 库存服务：查询、扣减、回补。
 * 扣减使用乐观锁防超卖。
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;
    private final IdService idService;

    public Page<StockVO> page(StockPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<Stock>()
                .eq(Stock::getShopId, shopId)
                .orderByDesc(Stock::getUpdateTime);

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            // goodsId/skuId 暂无法按名搜，此处留空，前端可二次过滤
        }
        if (Boolean.TRUE.equals(req.getWarnOnly())) {
            wrapper.apply("quantity <= warn_qty");
        }

        Page<Stock> page = stockMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<StockVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(s -> {
            StockVO vo = new StockVO();
            vo.setId(s.getId());
            vo.setGoodsId(s.getGoodsId());
            vo.setSkuId(s.getSkuId());
            vo.setQuantity(s.getQuantity());
            vo.setWarnQty(s.getWarnQty());
            vo.setAvgCost(s.getAvgCost());
            vo.setUpdateTime(s.getUpdateTime());
            return vo;
        }).toList());
        return result;
    }

    public StockVO getOrCreate(Long shopId, Long goodsId, Long skuId) {
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getShopId, shopId)
                        .eq(Stock::getGoodsId, goodsId)
                        .eq(Stock::getSkuId, skuId != null ? skuId : 0L)
                        .last("LIMIT 1"));

        if (stock == null) {
            stock = new Stock();
            stock.setId(idService.next("stock"));
            stock.setShopId(shopId);
            stock.setGoodsId(goodsId);
            stock.setSkuId(skuId != null ? skuId : 0L);
            stock.setQuantity(0);
            stock.setWarnQty(0);
            stock.setAvgCost(BigDecimal.ZERO);
            stockMapper.insert(stock);
        }
        StockVO vo = new StockVO();
        vo.setId(stock.getId());
        vo.setGoodsId(stock.getGoodsId());
        vo.setSkuId(stock.getSkuId());
        vo.setQuantity(stock.getQuantity());
        vo.setWarnQty(stock.getWarnQty());
        vo.setAvgCost(stock.getAvgCost());
        return vo;
    }

    /**
     * 库存扣减（乐观锁防超卖）。
     *
     * @param shopId  门店ID
     * @param goodsId 商品ID
     * @param skuId   SKU ID（可为空）
     * @param qty     扣减数量（正数）
     * @return 扣减后的剩余库存
     */
    @Transactional(rollbackFor = Exception.class)
    public int deduct(Long shopId, Long goodsId, Long skuId, int qty) {
        if (qty <= 0) {
            throw new BizException("扣减数量必须大于0");
        }
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getShopId, shopId)
                        .eq(Stock::getGoodsId, goodsId)
                        .eq(Stock::getSkuId, skuId != null ? skuId : 0L)
                        .last("LIMIT 1 FOR UPDATE"));

        if (stock == null) {
            throw new BizException("库存记录不存在");
        }
        if (stock.getQuantity() < qty) {
            throw new BizException("库存不足，当前库存：" + stock.getQuantity());
        }
        stock.setQuantity(stock.getQuantity() - qty);
        int updated = stockMapper.updateById(stock);
        if (updated == 0) {
            // 乐观锁冲突
            throw new BizException("库存并发扣减冲突，请重试");
        }
        return stock.getQuantity();
    }

    /**
     * 库存回补（订单取消/退款）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void revert(Long shopId, Long goodsId, Long skuId, int qty) {
        if (qty <= 0) {
            throw new BizException("回补数量必须大于0");
        }
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getShopId, shopId)
                        .eq(Stock::getGoodsId, goodsId)
                        .eq(Stock::getSkuId, skuId != null ? skuId : 0L)
                        .last("LIMIT 1"));

        if (stock == null) {
            // 不存在则创建
            getOrCreate(shopId, goodsId, skuId);
            stock = stockMapper.selectOne(
                    new LambdaQueryWrapper<Stock>()
                            .eq(Stock::getShopId, shopId)
                            .eq(Stock::getGoodsId, goodsId)
                            .eq(Stock::getSkuId, skuId != null ? skuId : 0L)
                            .last("LIMIT 1"));
        }
        stock.setQuantity(stock.getQuantity() + qty);
        int updated = stockMapper.updateById(stock);
        if (updated == 0) {
            throw new BizException("库存回补并发冲突，请重试");
        }
    }

    /**
     * 调整预警阈值。
     */
    public void updateWarnQty(Long id, Integer warnQty) {
        Stock stock = stockMapper.selectById(id);
        if (stock == null || !stock.getShopId().equals(currentShopId())) {
            throw new BizException("库存记录不存在");
        }
        stock.setWarnQty(warnQty);
        stockMapper.updateById(stock);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
