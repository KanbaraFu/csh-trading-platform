package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.secondhand.vo.CommentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author lenovo
* @description 针对表【comment(商品评论)】的数据库操作Mapper
* @createDate 2026-09-16 11:56:08
* @Entity com.campus.secondhand.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {
    //分页查询顶层评论（带用户信息）
    List<CommentVo> selectTopLevelComments(@Param("productId") Long productId,
                                           @Param("offset") Long offset,
                                           @Param("pageSize") Long pageSize);

    //查询当前商品的顶层评论的个数
    Long countTopLevelComments(@Param("productId")Long productId);

    //根据父评论数组查询子评论（带用户信息和被回复用户昵称）
    List<CommentVo> selectRepliesByParentIds(@Param("parentIds") List<Long> parentIds);
}




