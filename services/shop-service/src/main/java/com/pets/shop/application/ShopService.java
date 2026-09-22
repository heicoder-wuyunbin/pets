package com.pets.shop.application;

import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.shop.adapter.out.ShopMapper;
import com.pets.shop.application.dto.ShopDetailVO;
import com.pets.shop.application.dto.ShopUpdateReq;
import com.pets.shop.domain.entity.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 门店服务：详情查看 + 资料编辑。
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopMapper shopMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ShopDetailVO detail() {
        Long shopId = currentShopId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BizException("店铺不存在");
        }
        ShopDetailVO vo = new ShopDetailVO();
        vo.setId(shop.getId());
        vo.setShopName(shop.getShopName());
        vo.setLogo(shop.getLogo());
        vo.setBindPhone(shop.getBindPhone());
        vo.setLevel(shop.getLevel());
        vo.setExpireTime(shop.getExpireTime() != null ? shop.getExpireTime().format(DATE_FMT) : null);
        vo.setSmsRemain(shop.getSmsRemain());
        vo.setAddr(shop.getAddr());
        vo.setTelephone(shop.getTelephone());
        return vo;
    }

    public void update(ShopUpdateReq req) {
        Long shopId = currentShopId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BizException("店铺不存在");
        }
        shop.setShopName(req.getShopName());
        shop.setLogo(req.getLogo());
        shop.setAddr(req.getAddr());
        shop.setTelephone(req.getTelephone());
        shopMapper.updateById(shop);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
