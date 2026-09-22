package com.campus.secondhand.config;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.utils.TokenUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenUtil tokenUtil;

    public WebMvcConfig(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    /**
     * 登录拦截器的放行白名单：不需要登录就能访问的接口。
     * <p>商品列表/详情虽可游客访问，但因其与写操作路径相同，交由
     * {@link LoginInterceptor} 按 HTTP 方法区分，此处不列出。</p>
     */
    public static final String[] LOGIN_EXCLUDE_PATTERNS = {
        // 认证：注册、登录本身当然不需要登录态
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/code",
        "/api/auth/captcha",
        // 分类：首页要展示分类列表
        "/api/categories",
        "/api/categories/*",
        // 搜索：游客也能搜索商品
        "/api/search",
        "/api/search/**",
        // 统计：热门榜、浏览量上报不要求登录
        "/api/stat/hot-products",
        "/api/stat/view/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器：全局生效，白名单之外的接口都要求登录态
        addLoginInterceptor(registry, new LoginInterceptor(tokenUtil));
    }


    public void addLoginInterceptor(InterceptorRegistry registry, HandlerInterceptor loginInterceptor) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(Constants.API_ALL_PATTERN)
                .excludePathPatterns(LOGIN_EXCLUDE_PATTERNS)
                // order 越小越先执行，留好位置方便以后再加别的拦截器
                .order(0);
    }
}
