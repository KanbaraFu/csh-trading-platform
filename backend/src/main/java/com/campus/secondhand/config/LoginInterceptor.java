package com.campus.secondhand.config;

import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：解析请求头中的 token，把当前登录用户 id 注入 request 域。
 *
 * <p>后续 Controller 通过 {@code request.getAttribute("currentUserId")} 取用，
 * 避免把用户身份交给前端传参（防伪造）。</p>
 *
 * <p><b>游客可读的接口</b>：商品列表与详情（GET /api/products、GET /api/products/{id}）
 * 允许未登录访问，其余涉及商品的写操作（发布 / 编辑 / 下架 / 重新上架）都要求登录。
 * 这里按 HTTP 方法判断，而不是简单加到 excludePathPatterns —— 因为
 * {@code PUT/DELETE /api/products/{id}} 与 {@code GET /api/products/{id}} 路径完全相同，
 * 只靠路径无法区分读与写。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    /** request 域中存放当前登录用户 id 的 key */
    public static final String ATTR_CURRENT_USER_ID = "currentUserId";

    private final TokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 先尝试解析 token：游客可读接口若带了合法 token，也要能拿到身份。
        // 解析过程可能因 Redis 不可用等基础设施问题抛异常，
        // 此时应降级为「未登录」而非直接 500：游客可读接口照常放行。
        Long userId = null;
        try {
            String token = request.getHeader(Constants.TOKEN_HEADER);
            userId = tokenUtil.getUserIdAndRefresh(token);
        } catch (Exception e) {
            log.warn("token 解析失败，按未登录处理：{}", e.getMessage());
        }

        if (userId != null) {
            request.setAttribute(ATTR_CURRENT_USER_ID, userId);
            return true;
        }

        // 未登录：游客可读的 GET 接口放行，其余拒绝
        if (isGuestReadable(request)) {
            return true;
        }
        throw BizException.unauthorized("请先登录");
    }

    /**
     * 是否为「游客可读」的请求：GET 方式访问 /api/products 或 /api/products/{id}。
     * <p>不含 {@code /api/products/**} 子路径，避免把写操作误放行。</p>
     */
    private boolean isGuestReadable(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath))
                ? uri.substring(contextPath.length())
                : uri;
        return "/api/products".equals(path) || path.matches("^/api/products/[^/]+$");
    }
}
