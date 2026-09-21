package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 收货地址列表返回：records + 当前选中地址 id（序列化后为 selected_id）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressListVO {

    /**
     * 地址列表
     */
    private List<AddressVO> records;

    /**
     * 当前选中地址 id
     */
    private Long selectedId;

}
