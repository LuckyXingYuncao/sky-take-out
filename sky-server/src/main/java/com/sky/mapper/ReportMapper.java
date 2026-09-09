package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    /**
     * 统计指定时间范围内的营业额
     * @param begin 开始时间
     * @param end 结束时间
     * @return 营业额统计数据
     */
    List<Map<String, Object>> getTurnoverStatistics(@Param("begin") LocalDateTime begin,
                                                     @Param("end") LocalDateTime end);

    /**
     * 统计指定时间范围内的订单数据
     * @param begin 开始时间
     * @param end 结束时间
     * @return 订单统计数据
     */
    List<Map<String, Object>> getOrderStatistics(@Param("begin") LocalDateTime begin,
                                                  @Param("end") LocalDateTime end);

    /**
     * 统计指定时间范围内的用户数据
     * @param begin 开始时间
     * @param end 结束时间
     * @return 用户统计数据
     */
    List<Map<String, Object>> getUserStatistics(@Param("begin") LocalDateTime begin,
                                                 @Param("end") LocalDateTime end);

    /**
     * 销量排名TOP10
     * @param begin 开始时间
     * @param end 结束时间
     * @return 销量排名TOP10
     */
    List<GoodsSalesDTO> getTop10(@Param("begin") LocalDateTime begin,
                                  @Param("end") LocalDateTime end);
}