package com.pets.auth.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * 登录日志（高吞吐写入，TTL 90 天自动清理）。
 */
@Data
@Document(collection = "login_log")
public class LoginLog {

    @Id
    private String id;

    @Field("account_id")
    private Long accountId;

    @Field("clerk_id")
    private Long clerkId;

    @Field("shop_id")
    private Long shopId;

    private String phone;

    @Field("login_status")
    private Integer loginStatus;

    @Field("fail_reason")
    private String failReason;

    private String ip;

    @Field("user_agent")
    private String userAgent;

    @Indexed(expireAfterSeconds = 7776000)
    @Field("create_time")
    private Instant createTime;
}
