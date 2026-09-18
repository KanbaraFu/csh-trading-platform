package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.CommentDto;
import com.campus.secondhand.dto.CommentPageDto;
import com.campus.secondhand.pojo.Comment;
import com.campus.secondhand.service.CommentService;
import com.campus.secondhand.mapper.CommentMapper;
import com.campus.secondhand.utils.ExceptionUtil;
import com.campus.secondhand.vo.CommentVo;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author lenovo
* @description 针对表【comment(商品评论)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:08
*/
@Service
public class CommentServiceImpl extends CrudRepository<CommentMapper, Comment>
    implements CommentService{
    @Autowired
    private CommentMapper commentMapper;


    /**
     * 添加评论业务逻辑层
     * 1.判断各项是否为空，是否符合标准
     * 2.构造comment设置实体
     * + 2.1根据parent_id是否为零，设置reply_user_Id字段
     * 如果为0设置为空
     * 不为零，则根据parentid查询评论，判断父评论是否为空，
     * 父评论商品id是否与当前商品id相同
     * + 将其余赋值
     * 3.更新时间与主键自动更新不用管
     * 4.调用mapper方法传入comment，根据受影响行数判断是否成功，，返回vo对象
     * @param commentDto
     * @param userId
     */
    @Override
    public void createComment(CommentDto commentDto, Long userId) {
        //1.判断各项是否为空
        Long productId=commentDto.getProductId();
        ExceptionUtil.isTrue(productId==null,"商品的id不能为空");
        String content = commentDto.getContent();
        ExceptionUtil.isTrue(StringUtils.isBlank(content),"请输入评论内容");
        ExceptionUtil.isTrue(content.length()>=500,"评论的长度不能大于500字");
        Long parentId = commentDto.getParentId();
        //父id不可以为空，但是可以为0，表示为第一层评论
        ExceptionUtil.isTrue(parentId==null,"评论的父id不能为空");
        //2.创建Comment实体，根据父id是否为空进行赋值
        Comment comment=new Comment();
        if(parentId==0){
            comment.setParentId(0L);
            comment.setReplyUserId(null);
        }else {
            //当不为零时，根据父id查询相关评论
            //查询出父评论
            Comment commentparent = commentMapper.selectById(parentId);
            ExceptionUtil.isTrue(commentparent==null,"父id相关评论不存在");
            //父id所评论的商品是否与是当前所评论的商品
            ExceptionUtil.isTrue(!productId.equals(commentparent.getProductId()),"被回复的评论不属于当前商品");
            comment.setParentId(parentId);
            //设置字段（被回复的用户id）
            comment.setReplyUserId(commentparent.getUserId());
        }
        //设置剩下的其他字段
        comment.setUserId(userId);
        comment.setProductId(productId);
        comment.setContent(content);
        int row=commentMapper.insert(comment);
        ExceptionUtil.isTrue(row!=1,"添加评论失败");
    }

    /**
     * 分页查询得到出评论集合
     * **service层**
     * 1.取值，参数校验，判断productid是否为空
     * 根据当前产品id查询主评论，没有返回空的分页对象
     * 2.查看当前页的顶层评论（第几页pageNum，每页几条size）用offest表示第几条开始取（(pageNum-1)*pageSize）
     * 调用mapper层查询方法，传productId，offest，pagesize
     * 3.得到顶层评论的id数组，
     * 编写方法根据id数组查询他们的子评论
     * 4.拼接评论，包装返回PageResult
     * @param pageDto
     * @return
     */
    @Override
    public PageResult<CommentVo> getCommentsByProductId(CommentPageDto pageDto) {
        //1.取值
        Long productId = pageDto.getProductId();
        ExceptionUtil.isTrue(productId==null,"商品的Id不能为空");
        Long pageNum = pageDto.getPageNum();
        Long pageSize = pageDto.getPageSize();
        ExceptionUtil.isTrue(pageNum<=0||pageSize<=0,"查询的页码或每页的页数不正确");
        //通过productId查询评论总数，没有数据返回空
        Long total=commentMapper.countTopLevelComments(productId);
//        System.out.println("一共有数据："+total);
        if(total==0){
            //返回空的评论分页对象
            return PageResult.empty(pageNum,pageSize);
        }
        //2.分页查询当前的父评论数组(带用户信息)
        Long offset=(pageNum-1)*pageSize;
        List<CommentVo> topComments=commentMapper.selectTopLevelComments(productId,offset,pageSize);
        //3.获得父评论的id数组，根据id数组查询其子回复，普通查询
        List<Long> parentIds=new ArrayList<>();
        for (CommentVo topcommentVo: topComments) {
            parentIds.add(topcommentVo.getId());
        }
        //根据父id查询出所有的回复评论（顶层评论的回复评论）
        List<CommentVo> repliesComments=commentMapper.selectRepliesByParentIds(parentIds);
        //4.将评论拼接
        List<CommentVo> pageList=new ArrayList<>();
        for (CommentVo topCommentVo : topComments) {
            //添加主评论
            pageList.add(topCommentVo);
            for (CommentVo repliesCommentVo : repliesComments) {
                //添加该主评论对应的子评论
                if(repliesCommentVo.getParentId().equals(topCommentVo.getId())){
                    pageList.add(repliesCommentVo);
                }
            }
        }
        //5.将该分页查询数组封装为pageResult（定义好的对象返回）
        return  PageResult.of(pageList,total,pageNum,pageSize);
    }

    /**
     * 删除评论，如有子评论一同删除
     * 1.判断参数是否为空
     * 2.根据commentId查找对应评论，判断是不是当前用户删除，不是不能删
     * 3.递归查询子评论，放入数组，
     * 4.进行删除
     * @param id 评论的id
     * @param userId
     */
    @Override
    public void deleteComment(Long id, Long userId) {
        //接收参数判断
        Long commentId=id;
        ExceptionUtil.isTrue(commentId==null,"评论的id不能为空");
        ExceptionUtil.isTrue(userId == null, "用户id不能为空");
        // 2. 查询评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        ExceptionUtil.isTrue(comment == null, "评论不存在");
        //3.判断是否是当前的用户在删除当前用户的评论
        ExceptionUtil.isTrue(comment.getUserId().equals(userId),"仅能删除自己的评论");
        //4.查找其他评论，parentId为该评论id的即为子评论
        List<Long> childIds=new ArrayList<>();
        getAllChildIds(commentId,childIds);
        //5.如果有子评论，批量删除
        if(!childIds.isEmpty()){
            int row=commentMapper.deleteByIds(childIds);
            ExceptionUtil.isTrue(row!=childIds.size(),"相关子评论删除错误");
        }
        //6.删除自己
        int row2=commentMapper.deleteById(commentId);
        ExceptionUtil.isTrue(row2!=1,"删除主评论错误");
    }
    //递归查询所有子评论
    /**
     * 递归查询所有子评论id
     * @param id 父评论id
     * @return 所有后代评论id列表
     */
    public void getAllChildIds(Long id,List<Long> ids){
        //查询该评论的直接子评论id
        List<Long> childIds=commentMapper.selectChildIdsByParentId(id);
        //没有子评论就结束
        if(childIds==null||childIds.isEmpty()){
            return ;
        }
            //有子评论：把当前这批id加进去
            ids.addAll(childIds);
        //对每个子评论，继续调用自己，把它们的子评论也加进去
        for(Long childId:childIds){
            getAllChildIds(childId,ids);
        }
    }
}




