package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 订单条件搜索(分页)
     * @param ordersPageQueryDTO
     * @return
     */
    List<OrderVO> conditionSearch(@Param("dto") OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据ID查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 更新订单
     * @param orders
     */
    void update(Orders orders);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    List<Orders> getStatistics();
}