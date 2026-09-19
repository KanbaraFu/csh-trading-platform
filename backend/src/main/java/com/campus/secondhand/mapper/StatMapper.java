package com.campus.secondhand.mapper;

import com.campus.secondhand.vo.OverviewVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatMapper {
    /** 平台数据概览：一条聚合 SQL 统计 7 项 */
    OverviewVO selectOverviewStats();
}

