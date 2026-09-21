package com.campus.secondhand.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改收货地址入参：全部可选，传哪些改哪些（「设为默认」也走这里）。
 */
@Data
public class AddressUpdateDTO {

    private String receiverName;

    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的 11 位手机号")
    private String phone;

    private String region;

    private String detail;

    /**
     * true=设为默认，false=取消默认，null=不动
     */
    private Boolean isDefault;

}
