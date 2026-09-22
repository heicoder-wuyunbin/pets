package com.pets.common.context;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        AuthContextHolder.AuthContext ctx = new AuthContextHolder.AuthContext();
        ctx.setAccountId(parse(req.getHeader("X-User-Id")));
        ctx.setShopId(parse(req.getHeader("X-Shop-Id")));
        ctx.setChainGroupId(parse(req.getHeader("X-Chain-Group-Id")));
        ctx.setClerkId(parse(req.getHeader("X-Clerk-Id")));
        ctx.setRoleId(parse(req.getHeader("X-Role-Id")));
        AuthContextHolder.set(ctx);
        try {
            chain.doFilter(request, response);
        } finally {
            AuthContextHolder.clear();
        }
    }

    private static Long parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
