package com.campus.secondhand;

import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.CommentDto;
import com.campus.secondhand.dto.CommentPageDto;
import com.campus.secondhand.service.CommentService;
import com.campus.secondhand.vo.CommentVo;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@MapperScan("com.campus.secondhand.mapper")
public class CommentTest {

    @Autowired
    private CommentService commentService;

//    @Test
//    public void testComments() {
//        CommentDto commentDto = new CommentDto();
//        Long userId = 3L;
//        commentDto.setProductId(1L);
//        commentDto.setContent("朱少真帅");
//        commentDto.setParentId(0L);
//        commentService.createComment(commentDto, userId);
//    }

    //查总父评论数
    @Test
    public void testCountComments(){
        CommentPageDto pageDto = new CommentPageDto();
        pageDto.setProductId(1L);
        pageDto.setPageSize(1L);
        pageDto.setPageSize(10L);
        commentService.getCommentsByProductId(pageDto);
    }
}
