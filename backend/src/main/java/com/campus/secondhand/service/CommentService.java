package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.CommentDto;
import com.campus.secondhand.dto.CommentPageDto;
import com.campus.secondhand.pojo.Comment;
import com.campus.secondhand.vo.CommentVo;
import org.springframework.stereotype.Service;


/**
* @author lenovo
* @description 针对表【comment(商品评论)】的数据库操作Service
* @createDate 2026-09-16 11:56:08
*/
public interface CommentService extends IRepository<Comment> {

    void createComment(CommentDto commentDto, Long userId);

    /**
     * 分页查询商品评论（顶层评论分页，带回复）
     */
    PageResult<CommentVo> getCommentsByProductId(CommentPageDto pageDto);

    void deleteComment(Long commentId, Long userId);
}
