package com.pets.gateway.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴权白名单配置 (pets.auth.white-list).
 */
@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "pets.auth")
public class WhiteListProperties {

    private List<String> whiteList = new ArrayList<>();
}
