package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WebSocketServer webSocketServer;

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
        List<OrderDetail> orderDetailList = orderMapper.getOrderDetailListByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
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

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> cartList = shoppingCartMapper.listByUserId(userId);
        if (cartList == null || cartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        String orderNumber = String.valueOf(System.currentTimeMillis());

        Orders orders = Orders.builder()
                .number(orderNumber)
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(ordersSubmitDTO.getAddressBookId())
                .orderTime(LocalDateTime.now())
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .amount(ordersSubmitDTO.getAmount())
                .remark(ordersSubmitDTO.getRemark())
                .userName(addressBook.getConsignee())
                .phone(addressBook.getPhone())
                .address(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail())
                .consignee(addressBook.getConsignee())
                .estimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime())
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber())
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .packAmount(ordersSubmitDTO.getPackAmount())
                .build();

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(cart.getName())
                    .orderId(orders.getId())
                    .dishId(cart.getDishId())
                    .setmealId(cart.getSetmealId())
                    .dishFlavor(cart.getDishFlavor())
                    .number(cart.getNumber())
                    .amount(cart.getAmount())
                    .image(cart.getImage())
                    .build();
            orderDetailList.add(orderDetail);
        }
        orderMapper.insertOrderDetailBatch(orderDetailList);

        shoppingCartMapper.deleteByUserId(userId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        log.info("用户下单：{}", orderSubmitVO);
        return orderSubmitVO;
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders updateOrder = Orders.builder()
                .id(orders.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .payMethod(ordersPaymentDTO.getPayMethod())
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);

        OrderPaymentVO paymentVO = OrderPaymentVO.builder()
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .signType("MD5")
                .packageStr("prepay_id=mock_prepay_id")
                .build();
        log.info("订单支付：订单号={}，支付方式={}", ordersPaymentDTO.getOrderNumber(), ordersPaymentDTO.getPayMethod());
        webSocketServer.sendToAllClient("{\"type\":\"newOrder\",\"message\":\"您有新的订单，请及时处理\",\"orderNumber\":\""
                + ordersPaymentDTO.getOrderNumber() + "\"}");
        return paymentVO;
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);

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
        for (OrderVO vo : list) {
            List<OrderDetail> detailList = orderMapper.getOrderDetailListByOrderId(vo.getId());
            vo.setOrderDetailList(detailList);
        }
        Page<OrderVO> p = (Page<OrderVO>) list;
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        log.info("用户催单，订单ID={}", id);
    }

    @Override
    public void repetition(Long id) {
        List<OrderDetail> detailList = orderMapper.getOrderDetailListByOrderId(id);
        if (detailList == null || detailList.isEmpty()) {
            throw new RuntimeException("订单详情为空");
        }
        ShoppingCart cart = new ShoppingCart();
        for (OrderDetail detail : detailList) {
            BeanUtils.copyProperties(detail, cart);
            cart.setUserId(BaseContext.getCurrentId());
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(cart);
        }
        log.info("再来一单，原订单ID={}，共{}个商品", id, detailList.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"workspace:businessData", "workspace:orderOverview", "order:statistics"}, allEntries = true)
    public void userCancel(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders updateOrder = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
        log.info("用户取消订单，订单ID={}", id);
    }
}