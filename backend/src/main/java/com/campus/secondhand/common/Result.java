package com.campus.secondhand.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结构，所有 Controller 必须返回该类型。
 *
 * <pre>
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": {}
 * }
 * </pre>
 *
 * <p>约定：{@code code == 200} 表示成功，前端 {@code frontend/src/utils/request.js}
 * 会直接取 {@code data}；其它 code 会走错误提示（401 单独触发重新登录）。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码，取值见 {@link Constants} */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据，无数据时为 null */
    private T data;

    /* ==================== 成功 ==================== */

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> vo = new Result<>();
        vo.setCode(200);
        vo.setMessage("操作成功");
        vo.setData(data);
        return vo;
    }


    /* ==================== 失败 ==================== */

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> vo = new Result<>();
        vo.setCode(code);
        vo.setMessage(msg);
        return vo;
    }

    public static Result<Void> error(Integer code, String message) {
        return null;
    }

    public static Result<Void> error() {
        return null;
    }

    public static Result<Void> error(String s) {
        return null;
    }

    /**
     * 是否成功。注意这里是按 int 拆箱比较，code 为 null 时会先做空判断。
     */
    public boolean isSuccess() {
        return code != null && code == Constants.CODE_SUCCESS;
    }
}
