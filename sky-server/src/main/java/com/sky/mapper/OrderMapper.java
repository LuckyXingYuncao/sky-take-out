package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    List<OrderVO> conditionSearch(@Param("dto") OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders orders);

    List<Orders> getStatistics();

    @Insert("insert into orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, user_name, phone, address, consignee, estimated_delivery_time, delivery_status, tableware_number, tableware_status, pack_amount) " +
            "values (#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{checkoutTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{userName}, #{phone}, #{address}, #{consignee}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{tablewareNumber}, #{tablewareStatus}, #{packAmount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);

    void insertOrderDetailBatch(@Param("list") List<OrderDetail> list);

    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getOrderDetailListByOrderId(Long orderId);

    List<Orders> getByStatusAndOrderTimeBefore(@Param("status") Integer status, @Param("time") LocalDateTime time);

    List<Orders> getByStatusAndEstimatedDeliveryTimeBefore(@Param("status") Integer status, @Param("time") LocalDateTime time);
}