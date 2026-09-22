package com.pets.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置 (Java DSL, 类型安全).
 * 支持环境变量注入服务地址，默认 localhost 直连。
 */
@Configuration
public class RouteConfig {

    @Value("${auth.service.url:http://localhost:9001}")
    private String authUrl;
    @Value("${shop.service.url:http://localhost:9002}")
    private String shopUrl;
    @Value("${system.service.url:http://localhost:9003}")
    private String systemUrl;
    @Value("${trade.service.url:http://localhost:9004}")
    private String tradeUrl;
    @Value("${goods.service.url:http://localhost:9005}")
    private String goodsUrl;
    @Value("${stock.service.url:http://localhost:9006}")
    private String stockUrl;
    @Value("${member.service.url:http://localhost:9007}")
    private String memberUrl;
    @Value("${market.service.url:http://localhost:9008}")
    private String marketUrl;
    @Value("${report.service.url:http://localhost:9009}")
    private String reportUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri(authUrl))
                .route("shop-service", r -> r.path("/api/v1/shop/**", "/api/v1/notice/**")
                        .uri(shopUrl))
                .route("system-service", r -> r.path("/api/v1/clerk/**", "/api/v1/sys-param/**", "/api/v1/operation-log/**")
                        .uri(systemUrl))
                .route("trade-service", r -> r.path("/api/v1/trade/**")
                        .uri(tradeUrl))
                .route("goods-service", r -> r.path("/api/v1/goods/**", "/api/v1/service/**")
                        .uri(goodsUrl))
                .route("stock-service", r -> r.path("/api/v1/stock/**")
                        .uri(stockUrl))
                .route("member-service", r -> r.path("/api/v1/member/**")
                        .uri(memberUrl))
                .route("market-service", r -> r.path("/api/v1/marketing/**")
                        .uri(marketUrl))
                .route("report-service", r -> r.path("/api/v1/report/**")
                        .uri(reportUrl))
                .build();
    }
}
