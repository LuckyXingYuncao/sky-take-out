package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface WorkspaceMapper {

    /**
     * 查询今日运营数据
     * @param begin
     * @param end
     * @return
     */
    Map<String, Object> getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询菜品总览
     * @return
     */
    Map<String, Object> getDishOverview();

    /**
     * 查询套餐总览
     * @return
     */
    Map<String, Object> getSetmealOverview();

    /**
     * 查询订单管理数据
     * @return
     */
    Map<String, Object> getOrderOverview();
}