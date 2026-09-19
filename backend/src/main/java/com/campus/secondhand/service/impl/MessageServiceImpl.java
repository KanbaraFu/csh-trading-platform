package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.dto.CreateMessageDto;
import com.campus.secondhand.dto.QueryMessageDto;
import com.campus.secondhand.pojo.Message;
import com.campus.secondhand.service.MessageService;
import com.campus.secondhand.mapper.MessageMapper;
import com.campus.secondhand.utils.ExceptionUtil;
import com.campus.secondhand.vo.MessagePageVo;
import com.campus.secondhand.vo.MessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author lenovo
* @description 针对表【message(站内消息)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:08
*/
@Service
public class MessageServiceImpl extends CrudRepository<MessageMapper, Message>
    implements MessageService{
    @Autowired
    private MessageMapper messageMapper;
    /**
     * 分页条件查询信息
     1.接收参数，参数校验
     根据用户id查询消息总数，没有直接返回
     2.调用mapper层条件分页查询方法
     以及查各类消息数量的方法
     3.返回封装好的PageMessageVO对象（根据前端数据要求）
     * @param queryDto
     * @param userId
     * @return
     */
    @Override
    public MessagePageVo getMessages(QueryMessageDto queryDto, Long userId) {
        //1.接收参数判断
        ExceptionUtil.isTrue(userId==null,"用户的id不能为空");
        Long pageNum=queryDto.getPageNum();
        Long pageSize=queryDto.getPageSize();
        String type=queryDto.getType();
        //2.根据用户id与查询类型查询消息的总数量
        Long total = messageMapper.countMessages(userId, type);
        //4.查询各类消息的数量
        Long all=messageMapper.countMessages(userId,null);
        Long unread=messageMapper.countUnread(userId);
        Long system=messageMapper.countMessages(userId,"system");
        Long trade=messageMapper.countMessages(userId,"trade");
        Long comment=messageMapper.countMessages(userId,"comment");
        if(total==0){
            //为零返回空的分页查询对象
            return new MessagePageVo(new ArrayList<>(), 0L, pageNum, pageSize,
                    all, unread, system, trade, comment);
        }
        //3.分页条件查询查询信息
        //通过计算得到偏移量
        Long offset=(pageNum-1)*pageSize;
        List<MessageVo> records=messageMapper.selectMessages(userId,type,offset,pageSize);
        //5.将信息封装为MassagePageVo对象返回
        MessagePageVo messagePageVo = new MessagePageVo(records, total, pageNum, pageSize, all, unread, system, trade, comment);
        return messagePageVo;
    }

    /**
     * 更新单条消息为已读
     * @param id
     * @param userId
     */
    @Override
    public void readMessages(Long id, Long userId) {
        ExceptionUtil.isTrue(id==null,"信息的id不能为空");
        ExceptionUtil.isTrue(userId==null,"用户Id不能为空");
        //2.根据信息id更新单条消息为已读
        int row=messageMapper.updateReadStatus(id,userId);
        ExceptionUtil.isTrue(row!=1,"信息更新失败或信息已读");
    }

    /**
     * 更新所有消息为已读
     * @param type
     * @param userId
     */
    @Override
    public void readAllMessages(String type, Long userId) {
        ExceptionUtil.isTrue(userId==null,"用户Id不能为空");
        // 将 "all" 视为 null，标记全部类型
        if ("all".equals(type)) {
            type = null;
        }
        //根据type类型更新所有消息为已读
        messageMapper.updateAllReadStatus(userId, type);
    }

    /**
     * 当前用户未读信息数
     * @param userId
     * @return
     */
    @Override
    public Long getUnreadCount(Long userId) {
        //1.参数校验
        ExceptionUtil.isTrue(userId==null,"用户id不能为空");
        //2.查询未读数量
        return messageMapper.countUnread(userId);
    }

    /**
     * 创建信息方法
     * 参数校验，设置默认值，封装成对象
     * 调用mapper层添加方法
     * @param dto
     */
    @Override
    public void createMessage(CreateMessageDto dto) {
        //1.参数校验
        ExceptionUtil.isTrue(dto.getUserId()==null,"接收者id不能为空");
        ExceptionUtil.isTrue(dto.getType()==null,"消息类型不能为空");
        ExceptionUtil.isTrue(dto.getTitle()==null,"消息标题不能为空");
        ExceptionUtil.isTrue(dto.getContent()==null,"消息内容不能为空");
        //2.封装消息对象
        Message message = new Message();
        message.setUserId(dto.getUserId());
        message.setType(dto.getType());
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setBizId(dto.getBizId());
        message.setIsRead(0);  // 默认未读
        //3.插入数据库
        int row=messageMapper.insert(message);
        ExceptionUtil.isTrue(row!=1,"发送信息时发生错误");
    }
}




