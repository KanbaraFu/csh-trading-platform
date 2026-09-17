package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.pojo.Message;
import com.campus.secondhand.service.MessageService;
import com.campus.secondhand.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author lenovo
* @description 针对表【message(站内消息)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:08
*/
@Service
public class MessageServiceImpl extends CrudRepository<MessageMapper, Message>
    implements MessageService{

}




