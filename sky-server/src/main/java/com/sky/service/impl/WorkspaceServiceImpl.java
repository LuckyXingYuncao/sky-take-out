package com.sky.service.impl;

import com.sky.mapper.WorkspaceMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private WorkspaceMapper workspaceMapper;

    @Override
    @Cacheable(value = "workspace:businessData", key = "'today'")
    public BusinessDataVO getBusinessData() {
        log.info("查询今日运营数据");
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Map<String, Object> map = workspaceMapper.getBusinessData(begin, end);

        Double turnover = convertToDouble(map.get("turnover"));
        Integer validOrderCount = convertToInteger(map.get("validOrderCount"));
        Double orderCompletionRate = convertToDouble(map.get("orderCompletionRate"));
        Double unitPrice = convertToDouble(map.get("unitPrice"));
        Integer newUsers = convertToInteger(map.get("newUsers"));

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    @Cacheable(value = "workspace:dishOverview", key = "'current'")
    public DishOverViewVO getDishOverview() {
        log.info("查询菜品总览");
        Map<String, Object> map = workspaceMapper.getDishOverview();

        Integer sold = convertToInteger(map.get("sold"));
        Integer discontinued = convertToInteger(map.get("discontinued"));

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    @Cacheable(value = "workspace:setmealOverview", key = "'current'")
    public SetmealOverViewVO getSetmealOverview() {
        log.info("查询套餐总览");
        Map<String, Object> map = workspaceMapper.getSetmealOverview();

        Integer sold = convertToInteger(map.get("sold"));
        Integer discontinued = convertToInteger(map.get("discontinued"));

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    @Cacheable(value = "workspace:orderOverview", key = "'current'")
    public OrderOverViewVO getOrderOverview() {
        log.info("查询订单管理数据");
        Map<String, Object> map = workspaceMapper.getOrderOverview();

        Integer waitingOrders = convertToInteger(map.get("waitingOrders"));
        Integer deliveredOrders = convertToInteger(map.get("deliveredOrders"));
        Integer completedOrders = convertToInteger(map.get("completedOrders"));
        Integer cancelledOrders = convertToInteger(map.get("cancelledOrders"));
        Integer allOrders = convertToInteger(map.get("allOrders"));

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    private Double convertToDouble(Object obj) {
        if (obj == null) {
            return 0.0;
        }
        return Double.parseDouble(obj.toString());
    }

    private Integer convertToInteger(Object obj) {
        if (obj == null) {
            return 0;
        }
        return Integer.parseInt(obj.toString());
    }
}