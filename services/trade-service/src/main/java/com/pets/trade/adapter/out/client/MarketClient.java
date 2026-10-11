package com.pets.trade.adapter.out.client;

import com.pets.common.web.R;
import com.pets.trade.application.dto.MarketingCalcReq;
import com.pets.trade.application.dto.MarketingCalcResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * market-service 营销优惠计算 Feign 客户端。
 */
@FeignClient(name = "market-service", contextId = "marketClient", url = "${market.service.url:lb://market-service}")
public interface MarketClient {

    @PostMapping("/api/v1/marketing/calculate")
    R<MarketingCalcResult> calculate(@RequestBody MarketingCalcReq req);
}
