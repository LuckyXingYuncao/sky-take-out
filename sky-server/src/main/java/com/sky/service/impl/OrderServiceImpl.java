package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单条件搜索：{}", ordersPageQueryDTO);
        int page = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        PageHelper.startPage(page, pageSize);
        List<OrderVO> list = orderMapper.conditionSearch(ordersPageQueryDTO);
        Page<OrderVO> p = (Page<OrderVO>) list;
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        log.info("查询订单详情：id={}", id);
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        return orderVO;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"workspace:businessData", "workspace:orderOverview", "order:statistics"}, allEntries = true)
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单：{}", ordersConfirmDTO);
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(updateOrder);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"workspace:businessData", "workspace:orderOverview", "order:statistics"}, allEntries = true)
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单：{}", ordersRejectionDTO);
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"workspace:businessData", "workspace:orderOverview", "order:statistics"}, allEntries = true)
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        log.info("商家取消订单：{}", ordersCancelDTO);
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders updateOrder = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"workspace:businessData", "workspace:orderOverview", "order:statistics"}, allEntries = true)
    public void delivery(Long id) {
        log.info("派送订单：id={}", id);
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.CONFIRMED.equals(orders.getStatus())) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(updateOrder);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"workspace:businessData", "workspace:orderOverview", "order:statistics"}, allEntries = true)
    public void complete(Long id) {
        log.info("完成订单：id={}", id);
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
    }

    @Override
    @Cacheable(value = "order:statistics", key = "'current'")
    public OrderStatisticsVO getStatistics() {
        log.info("各个状态订单数量统计");
        List<Orders> list = orderMapper.getStatistics();
        Integer toBeConfirmed = 0;
        Integer confirmed = 0;
        Integer deliveryInProgress = 0;
        for (Orders orders : list) {
            Integer status = orders.getStatus();
            if (Orders.TO_BE_CONFIRMED.equals(status)) {
                toBeConfirmed++;
            } else if (Orders.CONFIRMED.equals(status)) {
                confirmed++;
            } else if (Orders.DELIVERY_IN_PROGRESS.equals(status)) {
                deliveryInProgress++;
            }
        }
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(toBeConfirmed);
        vo.setConfirmed(confirmed);
        vo.setDeliveryInProgress(deliveryInProgress);
        return vo;
    }
}