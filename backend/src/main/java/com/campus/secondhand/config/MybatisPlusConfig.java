package com.campus.secondhand.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.campus.secondhand.common.Constants;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.campus.secondhand.mapper")
public class MybatisPlusConfig {

    /**
     * 插件总入口，所有 InnerInterceptor 都挂在它上面。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 目前只有一个分页插件，后续新增的插件记得放在这一行之前
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 分页插件。
     *
     * <ul>
     *   <li>{@code setOverflow(false)}：页码超出总页数时返回空列表，而不是回到第一页，
     *       前端拿到空列表展示「暂无数据」更符合直觉。</li>
     *   <li>{@code setMaxLimit}：单页最多 100 条，防止 pageSize=999999 把库拖垮。</li>
     * </ul>
     */
    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        interceptor.setOverflow(false);
        interceptor.setMaxLimit(Constants.MAX_PAGE_SIZE);
        return interceptor;
    }
}
