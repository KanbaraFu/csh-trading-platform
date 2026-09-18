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
    public Result createComment(@RequestBody CommentDto commentDto,
                                @RequestHeader(value = Constants.TOKEN_HEADER, required = false)String token){
        //Long userId) 测试时用
        Long userId=tokenUtil.getUserId(token);
        if (userId == null) {
            return Result.error(Constants.CODE_UNAUTHORIZED, "请先登录");
        }
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

    /**
     * 删除评论（只能删自己的，子评论连带删除）
     * @param id 评论id（路径参数）
     * @param token 请求头中的token
     * @return 被删除的评论id
     */
    @DeleteMapping("/comments/{id}")
    public Result deleteComments(@PathVariable Long id,
                                   @RequestHeader(value = Constants.TOKEN_HEADER, required = false)String token){
        Long userId = tokenUtil.getUserId(token);
        if (userId == null) {
            return Result.error(Constants.CODE_UNAUTHORIZED, "请先登录");
        }
            commentServiceImpl.deleteComment(id, userId);
            return Result.success();
        }

}
