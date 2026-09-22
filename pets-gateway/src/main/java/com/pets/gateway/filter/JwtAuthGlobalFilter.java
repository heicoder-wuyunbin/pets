package com.pets.gateway.filter;

import com.pets.gateway.properties.WhiteListProperties;
import com.pets.gateway.util.RouteUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * JWT 鉴权全局过滤器 (Reactive).
 * <p>
 * 校验白名单放行; 否则解析 Bearer token, 注入下游用户上下文头, 并生成/传递 traceId.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JwtAuthGlobalFilter implements WebFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TRACE_HEADER = "X-Trace-Id";

    private final WhiteListProperties whiteListProperties;
    private final SecretKey secretKey;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthGlobalFilter(WhiteListProperties whiteListProperties,
                               @Value("${pets.jwt.secret}") String secret) {
        this.whiteListProperties = whiteListProperties;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();

        // 白名单放行
        for (String pattern : whiteListProperties.getWhiteList()) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 提取 Authorization 头
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return RouteUtil.unauthorized(exchange.getResponse());
        }
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // 解析 token
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return RouteUtil.unauthorized(exchange.getResponse());
        }

        // 提取 claims 并注入下游请求头
        String userId = claims.getSubject();
        Long shopId = claims.get("shop_id", Long.class);
        Long chainGroupId = claims.get("chain_group_id", Long.class);
        Long clerkId = claims.get("clerk_id", Long.class);
        Long roleId = claims.get("role_id", Long.class);

        // 生成或传递 traceId
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }

        ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-Trace-Id", traceId);
        addHeaderIfNotNull(builder, "X-Shop-Id", shopId);
        addHeaderIfNotNull(builder, "X-Chain-Group-Id", chainGroupId);
        addHeaderIfNotNull(builder, "X-Clerk-Id", clerkId);
        addHeaderIfNotNull(builder, "X-Role-Id", roleId);

        ServerHttpRequest mutated = builder.build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private void addHeaderIfNotNull(ServerHttpRequest.Builder builder, String name, Long value) {
        if (value != null) {
            builder.header(name, String.valueOf(value));
        }
    }
}
