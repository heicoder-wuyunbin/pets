package com.pets.auth.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * 短信验证码留痕（实时校验走 Redis，此处仅审计留痕）。
 */
@Data
@Document(collection = "sms_code")
public class SmsCode {

    @Id
    private String id;

    private String phone;

    @Field("biz_type")
    private String bizType;

    @Field("code_hash")
    private String codeHash;

    private String ip;

    @Indexed(expireAfterSeconds = 0)
    @Field("expire_at")
    private Instant expireAt;

    private boolean used;

    @Field("used_at")
    private Instant usedAt;

    @Field("create_time")
    private Instant createTime;
}
