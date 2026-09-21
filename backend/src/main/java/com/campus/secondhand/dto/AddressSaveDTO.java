package com.campus.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 新增收货地址入参。
 */
@Data
public class AddressSaveDTO {

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的 11 位手机号")
    private String phone;

    @NotBlank(message = "所在区域不能为空")
    private String region;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /**
     * 是否设为默认（前端 switch 传 Boolean）
     */
    private Boolean isDefault;

}
