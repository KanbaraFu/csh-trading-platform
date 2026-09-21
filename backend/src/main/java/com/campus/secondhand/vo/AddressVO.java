package com.campus.secondhand.vo;

import lombok.Data;

/**
 * 收货地址返回对象。
 * 全局 Jackson 为 SNAKE_CASE，序列化后即为 receiver_name / is_default 等前端契约字段。
 */
@Data
public class AddressVO {

    /**
     * 地址ID
     */
    private Long id;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 校区 / 宿舍楼等大区域
     */
    private String region;

    /**
     * 详细门牌
     */
    private String detail;

    /**
     * 是否默认 0否 1默认
     */
    private Integer isDefault;

}
