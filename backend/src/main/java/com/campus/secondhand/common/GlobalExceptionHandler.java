package com.campus.secondhand.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

/**
 * 全局异常处理器：把异常统一转成 {@link Result}，保证前端永远拿到同一套结构。
 *
 * <p>重要约定：这里全部返回 HTTP 200，错误码放在响应体的 {@code code} 字段里。
 * 因为前端 {@code frontend/src/utils/request.js} 是在 axios 的「成功回调」里解析
 * {@code response.data.code}，如果这里返回 HTTP 4xx/5xx，axios 会走失败回调，
 * 前端就拿不到 message 做提示了（401 也会无法触发重新登录逻辑）。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：可预期，按 warn 级别记录，不打堆栈，避免污染日志。
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * @RequestBody + @Valid 校验失败。
     * 注意：MethodArgumentNotValidException 是 BindException 的子类，
     * Spring 会优先匹配更具体的这里。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = firstErrorMessage(e.getBindingResult().getFieldErrors());
        log.warn("参数校验失败: {}", message);
        return Result.error(Constants.CODE_BAD_REQUEST, message);
    }

    /**
     * 表单 / URL 参数绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = firstErrorMessage(e.getBindingResult().getFieldErrors());
        log.warn("参数绑定失败: {}", message);
        return Result.error(Constants.CODE_BAD_REQUEST, message);
    }

    /**
     * 缺少必填的请求参数，如 /api/search 没传 keyword。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "缺少必填参数：" + e.getParameterName();
        log.warn("缺少请求参数: {}", message);
        return Result.error(Constants.CODE_BAD_REQUEST, message);
    }

    /**
     * 参数类型不匹配，如 /api/products/abc 传了非数字的 id。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "参数格式不正确：" + e.getName();
        log.warn("参数类型不匹配: {}", message);
        return Result.error(Constants.CODE_BAD_REQUEST, message);
    }

    /**
     * 请求体不是合法 JSON，如前端 JSON 拼错、字段类型不对。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(Constants.CODE_BAD_REQUEST, "请求参数格式不正确");
    }

    /**
     * 请求方法不支持，如用 GET 调了 POST 接口。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return Result.error(Constants.CODE_BAD_REQUEST, "请求方法不支持：" + e.getMethod());
    }

    /**
     * 兜底异常：未预期的错误，打堆栈方便排查；对外不暴露内部细节。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error();
    }

    /**
     * 取第一条字段校验错误信息，取不到时给一个兜底文案。
     */
    private String firstErrorMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");
    }
}
