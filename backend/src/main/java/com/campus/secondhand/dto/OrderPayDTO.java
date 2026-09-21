package com.campus.secondhand.dto;


import com.campus.secondhand.common.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 支付参数（表示支付的方式）
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPayDTO {

    @JsonProperty("payMethod")
    private String payMethod;
}
