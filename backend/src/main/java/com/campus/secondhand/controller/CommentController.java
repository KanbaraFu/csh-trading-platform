package com.campus.secondhand.controller;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.CommentDto;
import com.campus.secondhand.dto.CommentPageDto;
import com.campus.secondhand.pojo.Comment;
import com.campus.secondhand.service.CommentService;
import com.campus.secondhand.service.impl.CommentServiceImpl;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 评论controller层
 */
@RestController
@RequestMapping("/api")
public class CommentController {
    @Autowired
    private CommentService commentServiceImpl;
    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 添加评论方法
     * @param commentDto
     * @return
     */
    @PostMapping("/comments")
    //先不从taken取userId
    public Result createComment(@RequestBody CommentDto commentDto, Long userId) {
        System.out.println(commentDto);
//        @RequestHeader(value = Constants.TOKEN_HEADER, required = false)String token)
// 用户id待获取（token获取）
//        Long userId = tokenUtil.getUserId(token);
        commentServiceImpl.createComment(commentDto,userId);
        return Result.success();
    }


    /**
     * 分页查询评论
     * 1.dto对象接收数据
     * 2.分页对象pageresult里的泛型装commentvo对象，然后Result的泛型装分页对象
     * 3.返回Result<PageResult<CommentVo>>类型数据
     */
    @GetMapping("/products/{id}/comments")
    public Result<PageResult<CommentVo>> getComments(@PathVariable Long id, CommentPageDto commentPageDto){
        commentPageDto.setProductId(id);
        PageResult<CommentVo> result=commentServiceImpl.getCommentsByProductId(commentPageDto);
        return Result.success(result);
    }
}
