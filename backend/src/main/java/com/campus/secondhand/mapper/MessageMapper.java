package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.secondhand.vo.MessageVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author lenovo
* @description 针对表【message(站内消息)】的数据库操作Mapper
* @createDate 2026-09-16 11:56:08
* @Entity com.campus.secondhand.entity.Message
*/
public interface MessageMapper extends BaseMapper<Message> {
    /**
     * 查询消息总数（可按类型筛选）
     */
    Long countMessages(@Param("userId") Long userId, @Param("type") String type);
    /**
     * 分页查询消息（可按类型筛选）
     * @param userId 用户id
     * @param type 消息类型（可选）
     * @param offset 偏移量
     * @param pageSize 每页条数
     * @return 消息列表
     */
    List<MessageVo> selectMessages(@Param("userId") Long userId,
                                   @Param("type") String type,
                                   @Param("offset") Long offset,
                                   @Param("pageSize") Long pageSize);


    /**
     * 查询未读消息数量(查询信息时已经写了查询未读信息数的方法)
     */
    Long countUnread(@Param("userId") Long userId);

    /**
     * 更新单条信息为已读
     * @param id
     * @param userId
     * @return
     */
    int updateReadStatus(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量将消息标记为已读
     * @param userId 用户id
     * @param type 消息类型（可选，不传则标记所有类型）
     * @return 更新行数
     */
    int updateAllReadStatus(@Param("userId") Long userId, @Param("type") String type);
}




