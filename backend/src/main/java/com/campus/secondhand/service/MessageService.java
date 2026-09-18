package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.dto.CreateMessageDto;
import com.campus.secondhand.dto.QueryMessageDto;
import com.campus.secondhand.pojo.Message;
import com.campus.secondhand.vo.MessagePageVo;

/**
* @author lenovo
* @description 针对表【message(站内消息)】的数据库操作Service
* @createDate 2026-09-16 11:56:08
*/
public interface MessageService extends IRepository<Message> {

    MessagePageVo getMessages(QueryMessageDto queryDto, Long userId);

    void readMessages(Long id, Long userId);

    void readAllMessages(String type, Long userId);

    Long getUnreadCount(Long userId);

    void createMessage(CreateMessageDto dto);
}
