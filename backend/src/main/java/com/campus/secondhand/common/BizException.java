package com.campus.secondhand.common;

import lombok.Getter;

/**
 * 业务异常：Service 层校验不通过时抛出，由 {@link GlobalExceptionHandler} 统一转成 {@link Result}。
 *
 * <p>用法示例：</p>
 * <pre>
 * if (user == null) {
 *     throw BizException.notFound("用户不存在");
 * }
 * </pre>
 *
 * <p>该类继承 {@link RuntimeException}，属于非受检异常，不会污染方法签名；
 * 由于是运行时抛出，注意<b>不要</b>在事务方法里随便捕获后吞掉，否则事务不会回滚。</p>
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，默认 500 */
    private final Integer code;

    public BizException(String message) {
        this(Constants.CODE_ERROR, message);
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /* ==================== 常用快捷构造 ==================== */

    /** 400 参数错误 / 业务校验不通过 */
    public static BizException badRequest(String message) {
        return new BizException(Constants.CODE_BAD_REQUEST, message);
    }

    /** 401 未登录 / Token 失效 */
    public static BizException unauthorized(String message) {
        return new BizException(Constants.CODE_UNAUTHORIZED, message);
    }

    /** 403 无权限操作该数据 */
    public static BizException forbidden(String message) {
        return new BizException(Constants.CODE_FORBIDDEN, message);
    }

    /** 404 资源不存在 */
    public static BizException notFound(String message) {
        return new BizException(Constants.CODE_NOT_FOUND, message);
    }
}
