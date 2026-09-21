package com.campus.secondhand.controller;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.CreateMessageDto;
import com.campus.secondhand.dto.QueryMessageDto;
import com.campus.secondhand.service.MessageService;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.MessagePageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MessageController {
    @Autowired
    private MessageService messageServiceImpl;
    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 分页条件查询消息信息
     *
    * @param queryDto
    * @param token
     * @return
     */
    @GetMapping("/messages")
    public Result<MessagePageVo> getMessage(QueryMessageDto queryDto,
//                                            Long userId){
                           @RequestHeader(value = Constants.TOKEN_HEADER, required = false)String token){
        Long userId=tokenUtil.getUserId(token);
        if (userId == null) {
            return Result.error(Constants.CODE_UNAUTHORIZED, "请先登录");
        }
        MessagePageVo result = messageServiceImpl.getMessages(queryDto, userId);
        return Result.success(result);
    }

    /**
     * 使单条信息更新为已读状态
     * @param id
     * @param token
     * @return
     */
    @PutMapping("/messages/{id}/read")
    public Result readMessage(@PathVariable Long id,
            @RequestHeader(value = Constants.TOKEN_HEADER, required = false)String token){
        Long userId=tokenUtil.getUserId(token);
        if (userId == null) {
            return Result.error(Constants.CODE_UNAUTHORIZED, "请先登录");
        }
        messageServiceImpl.readMessages(id,userId);
        return Result.success();
    }

    /**
     * 更新所有信息为已读
     * @param body
     * @param token
     * @return
     */
    @PutMapping("/messages/read-all")
    public Result readAllMessages(@RequestBody Map<String, Object> body,
                                   @RequestHeader(value = Constants.TOKEN_HEADER, required = false)String token){
        Long userId=tokenUtil.getUserId(token);
        if (userId == null) {
            return Result.error(Constants.CODE_UNAUTHORIZED, "请先登录");
        }
        String type=null;
        if(body!=null){
            type = (String) body.get("type");
        }
        messageServiceImpl.readAllMessages(type,userId);
        return Result.success();
    }
    /**
     * 查询未读消息数量
     * @param token 请求头中的token
     * @return 未读数量
     */
    @GetMapping("/messages/unread-count")
    public Result<Map<String, Long>> getUnreadCount(
            @RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token) {
        Long userId = tokenUtil.getUserId(token);
        if (userId == null) {
            return Result.error(Constants.CODE_UNAUTHORIZED, "请先登录");
        }
        Long count = messageServiceImpl.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    /**
     * 创建消息
     * @param dto 消息参数（userId, type, title, content, bizId）
     * @return 创建的消息id
     */
    @PostMapping("/messages")
    public Result createMessage(@RequestBody CreateMessageDto dto) {
        messageServiceImpl.createMessage(dto);
        return Result.success();
    }
}
