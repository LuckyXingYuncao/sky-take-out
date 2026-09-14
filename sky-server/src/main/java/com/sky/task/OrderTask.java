package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时未支付的订单
     * 每分钟执行一次，将下单超过15分钟仍未支付的订单自动取消
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processTimeoutPaymentOrders() {
        log.info("定时任务：开始处理超时未支付订单...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeBefore(
                Orders.PENDING_PAYMENT, threshold);

        if (ordersList == null || ordersList.isEmpty()) {
            log.info("定时任务：没有需要处理的超时未支付订单");
            return;
        }

        for (Orders orders : ordersList) {
            Orders updateOrder = Orders.builder()
                    .id(orders.getId())
                    .status(Orders.CANCELLED)
                    .cancelReason("订单超时未支付，系统自动取消")
                    .cancelTime(LocalDateTime.now())
                    .build();
            orderMapper.update(updateOrder);
            log.info("定时任务：自动取消超时未支付订单，订单号={}", orders.getNumber());
        }
        log.info("定时任务：处理超时未支付订单完成，共处理{}条", ordersList.size());
    }

    /**
     * 处理派送超时的订单
     * 每隔5分钟执行一次，将超过预计送达时间2小时仍未完成的订单自动完成
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    public void processTimeoutDeliveryOrders() {
        log.info("定时任务：开始处理派送超时订单...");
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        List<Orders> ordersList = orderMapper.getByStatusAndEstimatedDeliveryTimeBefore(
                Orders.DELIVERY_IN_PROGRESS, threshold);

        if (ordersList == null || ordersList.isEmpty()) {
            log.info("定时任务：没有需要处理的派送超时订单");
            return;
        }

        for (Orders orders : ordersList) {
            Orders updateOrder = Orders.builder()
                    .id(orders.getId())
                    .status(Orders.COMPLETED)
                    .deliveryTime(LocalDateTime.now())
                    .build();
            orderMapper.update(updateOrder);
            log.info("定时任务：自动完成派送超时订单，订单号={}", orders.getNumber());
        }
        log.info("定时任务：处理派送超时订单完成，共处理{}条", ordersList.size());
    }
}