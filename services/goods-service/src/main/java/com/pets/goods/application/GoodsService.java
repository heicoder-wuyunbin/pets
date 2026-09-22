package com.pets.goods.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.goods.adapter.out.GoodsMapper;
import com.pets.goods.adapter.out.GoodsSkuMapper;
import com.pets.goods.application.dto.GoodsPageReq;
import com.pets.goods.application.dto.GoodsSaveReq;
import com.pets.goods.application.dto.GoodsSkuVO;
import com.pets.goods.application.dto.GoodsVO;
import com.pets.goods.domain.entity.Goods;
import com.pets.goods.domain.entity.GoodsSku;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务：分页/保存（含SKU）/上下架。
 */
@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsSkuMapper skuMapper;
    private final IdService idService;

    public Page<GoodsVO> page(GoodsPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getShopId, shopId)
                .orderByDesc(Goods::getCreateTime);

        if (req.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, req.getCategoryId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Goods::getName, req.getKeyword())
                    .or().like(Goods::getBarcode, req.getKeyword()));
        }
        if (req.getStatus() != null) {
            wrapper.eq(Goods::getStatus, req.getStatus());
        }

        Page<Goods> page = goodsMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<GoodsVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public GoodsVO detail(Long id) {
        Goods goods = getOwnedGoods(id);
        return toVO(goods);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(GoodsSaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            Goods goods = new Goods();
            goods.setId(idService.next("goods"));
            goods.setShopId(shopId);
            goods.setCategoryId(req.getCategoryId());
            goods.setName(req.getName());
            goods.setBarcode(req.getBarcode());
            goods.setUnit(req.getUnit());
            goods.setRetailPrice(req.getRetailPrice());
            goods.setCostPrice(req.getCostPrice());
            goods.setPic(req.getPic());
            goods.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            goodsMapper.insert(goods);
            // 保存 SKU
            saveSkus(goods.getId(), req.getSkus());
        } else {
            Goods goods = getOwnedGoods(req.getId());
            goods.setCategoryId(req.getCategoryId());
            goods.setName(req.getName());
            goods.setBarcode(req.getBarcode());
            goods.setUnit(req.getUnit());
            goods.setRetailPrice(req.getRetailPrice());
            goods.setCostPrice(req.getCostPrice());
            goods.setPic(req.getPic());
            goods.setStatus(req.getStatus());
            goodsMapper.updateById(goods);
            // 重建 SKU：先删后增
            skuMapper.delete(new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, goods.getId()));
            saveSkus(goods.getId(), req.getSkus());
        }
    }

    public void updateStatus(Long id, Integer status) {
        Goods goods = getOwnedGoods(id);
        goods.setStatus(status);
        goodsMapper.updateById(goods);
    }

    private void saveSkus(Long goodsId, List<GoodsSaveReq.GoodsSkuReq> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        for (GoodsSaveReq.GoodsSkuReq s : skus) {
            GoodsSku sku = new GoodsSku();
            sku.setId(idService.next("goods_sku"));
            sku.setGoodsId(goodsId);
            sku.setSpecName(s.getSpecName());
            sku.setPrice(s.getPrice());
            skuMapper.insert(sku);
        }
    }

    private GoodsVO toVO(Goods g) {
        GoodsVO vo = new GoodsVO();
        vo.setId(g.getId());
        vo.setCategoryId(g.getCategoryId());
        vo.setName(g.getName());
        vo.setBarcode(g.getBarcode());
        vo.setUnit(g.getUnit());
        vo.setRetailPrice(g.getRetailPrice());
        vo.setCostPrice(g.getCostPrice());
        vo.setPic(g.getPic());
        vo.setStatus(g.getStatus());
        vo.setCreateTime(g.getCreateTime());
        // SKU 列表
        List<GoodsSku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<GoodsSku>().eq(GoodsSku::getGoodsId, g.getId()));
        vo.setSkus(skus.stream().map(s -> {
            GoodsSkuVO svo = new GoodsSkuVO();
            svo.setId(s.getId());
            svo.setGoodsId(s.getGoodsId());
            svo.setSpecName(s.getSpecName());
            svo.setPrice(s.getPrice());
            return svo;
        }).toList());
        return vo;
    }

    private Goods getOwnedGoods(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        if (!goods.getShopId().equals(currentShopId())) {
            throw new BizException("无权操作该商品");
        }
        return goods;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
