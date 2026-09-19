package com.campus.secondhand.config;

import com.campus.secondhand.common.Constants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 登录拦截器的放行白名单：不需要登录就能访问的接口。
     */
    public static final String[] LOGIN_EXCLUDE_PATTERNS = {
        // 认证：注册、登录本身当然不需要登录态
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/code",
        // 分类：首页要展示分类列表
        "/api/categories",
        "/api/categories/*",
        // 商品：游客也能浏览和搜索商品
        "/api/products",
        "/api/products/*",
        // 搜索：游客也能搜索商品
        "/api/search",
        "/api/search/**",
        // 统计：热门榜、浏览量上报不要求登录
        "/api/stat/hot-products",
        "/api/stat/view/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO 登录拦截器实现后，取消下面这行注释即可全局生效：
        // addLoginInterceptor(registry, new LoginInterceptor());
    }


    public void addLoginInterceptor(InterceptorRegistry registry, HandlerInterceptor loginInterceptor) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(Constants.API_ALL_PATTERN)
                .excludePathPatterns(LOGIN_EXCLUDE_PATTERNS)
                // order 越小越先执行，留好位置方便以后再加别的拦截器
                .order(0);
    }
}
