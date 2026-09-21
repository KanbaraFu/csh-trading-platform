package com.campus.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 评论展示对象（含用户信息），查评论，返回给前端的评论对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVo {
    /**评论的id**/
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 评论者用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 父评论ID，0表示顶层评论 */
    private Long parentId;

    /** 被回复的用户ID */
    private Long replyUserId;

    /** 创建时间 */
    private Date createTime;

    // ========== 用户信息 ==========

    private String nickname;//评论人的昵称

    private String avatar;//评论人的头像

    //=======根据是否父评论决定值的可选字段=====

    private String replyNickname;//被回复人的昵称
}