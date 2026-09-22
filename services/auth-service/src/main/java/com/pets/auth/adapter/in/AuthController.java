package com.pets.auth.adapter.in;

import com.pets.auth.application.AuthService;
import com.pets.auth.application.dto.LoginReq;
import com.pets.auth.application.dto.LoginResp;
import com.pets.auth.application.dto.RegisterReq;
import com.pets.auth.application.dto.ResetPasswordReq;
import com.pets.auth.application.dto.SelectShopReq;
import com.pets.auth.application.dto.SendCodeReq;
import com.pets.common.exception.BizException;
import com.pets.common.web.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return R.ok(authService.login(req));
    }

    @PostMapping("/select-shop")
    public R<LoginResp> selectShop(@Valid @RequestBody SelectShopReq req, HttpServletRequest httpRequest) {
        Long accountId = parseAccountId(httpRequest);
        return R.ok(authService.selectShop(req, accountId));
    }

    @PostMapping("/register")
    public R<LoginResp> register(@Valid @RequestBody RegisterReq req) {
        return R.ok(authService.register(req));
    }

    @PostMapping("/reset-password")
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return R.ok();
    }

    @PostMapping("/send-code")
    public R<Void> sendCode(@Valid @RequestBody SendCodeReq req) {
        authService.sendCode(req);
        return R.ok();
    }

    private Long parseAccountId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isBlank()) {
            throw new BizException("未登录或登录已过期");
        }
        try {
            return Long.valueOf(header);
        } catch (NumberFormatException e) {
            throw new BizException("用户标识非法");
        }
    }
}
