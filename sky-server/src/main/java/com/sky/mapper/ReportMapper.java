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
     * @param begin
     * @param end
     * @return
     */
    List<Map<String, Object>> getTurnoverStatistics(@Param("begin") LocalDateTime begin,
                                                     @Param("end") LocalDateTime end);

    /**
     * 统计指定时间范围内的订单数据
     * @param begin
     * @param end
     * @return
     */
    List<Map<String, Object>> getOrderStatistics(@Param("begin") LocalDateTime begin,
                                                  @Param("end") LocalDateTime end);

    /**
     * 统计指定时间范围内的用户数据
     * @param begin
     * @param end
     * @return
     */
    List<Map<String, Object>> getUserStatistics(@Param("begin") LocalDateTime begin,
                                                 @Param("end") LocalDateTime end);

    /**
     * 销量排名TOP10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getTop10(@Param("begin") LocalDateTime begin,
                                  @Param("end") LocalDateTime end);
}