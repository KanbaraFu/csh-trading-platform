package com.campus.secondhand.config;

import com.campus.secondhand.common.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带 Cookie / Authorization 之类的凭证
        config.setAllowCredentials(true);
        // 用 allowedOriginPatterns 而不是 allowedOrigins，才能和 allowCredentials 共存
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 预检请求（OPTIONS）结果缓存 1 小时，减少重复预检
        config.setMaxAge(3600L);
        // 暴露 token 响应头，方便前端在需要时直接读取（本项目 token 走响应体，属兜底）
        config.addExposedHeader(Constants.TOKEN_HEADER);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
