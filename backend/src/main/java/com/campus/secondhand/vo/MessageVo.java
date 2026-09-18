package com.campus.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 消息展示对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageVo {

    /** 消息ID */
    private Long id;

    /** 消息类型：system/trade/comment */
    private String type;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 关联业务id（商品id/订单id） */
    private Long bizId;

    /** 0未读 1已读 */
    @JsonProperty("is_read")
    private Integer isRead;

    /** 创建时间 */
    private Date createTime;
// ========== 关联商品信息（可为空） ==========
    /** 关联商品封面图 */
    private String productCover;

    /** 关联商品标题 */
    private String productTitle;
}
