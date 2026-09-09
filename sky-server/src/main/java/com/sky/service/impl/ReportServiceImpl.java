package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("营业额统计：begin={}, end={}", begin, end);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> list = reportMapper.getTurnoverStatistics(beginTime, endTime);
        Map<Object, Object> dateMap = list.stream()
                .collect(Collectors.toMap(m -> m.get("orderDate"), m -> m.get("turnover")));

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(begin, end);
        for (long i = 0; i <= days; i++) {
            LocalDate date = begin.plusDays(i);
            String dateStr = date.toString();
            dateList.add(dateStr);
            Object turnover = dateMap.get(dateStr);
            turnoverList.add(turnover != null ? turnover.toString() : "0");
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        log.info("订单统计：begin={}, end={}", begin, end);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> list = reportMapper.getOrderStatistics(beginTime, endTime);
        Map<Object, Map<String, Object>> dateMap = list.stream()
                .collect(Collectors.toMap(m -> m.get("orderDate"), m -> m));

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;

        long days = ChronoUnit.DAYS.between(begin, end);
        for (long i = 0; i <= days; i++) {
            LocalDate date = begin.plusDays(i);
            String dateStr = date.toString();
            dateList.add(dateStr);

            Map<String, Object> map = dateMap.get(dateStr);
            long total = map != null ? ((Number) map.get("totalOrderCount")).longValue() : 0;
            long valid = map != null ? ((Number) map.get("validOrderCount")).longValue() : 0;

            orderCountList.add(String.valueOf(total));
            validOrderCountList.add(String.valueOf(valid));
            totalOrderCount += total;
            validOrderCount += valid;
        }

        double orderCompletionRate = totalOrderCount > 0
                ? (double) validOrderCount / totalOrderCount : 0.0;
        orderCompletionRate = Math.round(orderCompletionRate * 100.0) / 100.0;

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        log.info("用户统计：begin={}, end={}", begin, end);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> list = reportMapper.getUserStatistics(beginTime, endTime);
        Map<Object, Map<String, Object>> dateMap = list.stream()
                .collect(Collectors.toMap(m -> m.get("userDate"), m -> m));

        List<String> dateList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();

        long days = ChronoUnit.DAYS.between(begin, end);
        for (long i = 0; i <= days; i++) {
            LocalDate date = begin.plusDays(i);
            String dateStr = date.toString();
            dateList.add(dateStr);

            Map<String, Object> map = dateMap.get(dateStr);
            long total = map != null ? ((Number) map.get("totalUserCount")).longValue() : 0;
            long newUser = map != null ? ((Number) map.get("newUserCount")).longValue() : 0;

            totalUserList.add(String.valueOf(total));
            newUserList.add(String.valueOf(newUser));
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        log.info("销量排名TOP10：begin={}, end={}", begin, end);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> list = reportMapper.getTop10(beginTime, endTime);

        List<String> nameList = list.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());

        List<String> numberList = list.stream()
                .map(dto -> String.valueOf(dto.getNumber()))
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        log.info("导出Excel报表");
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> list = reportMapper.getTurnoverStatistics(beginTime, endTime);

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream os = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("运营数据报表");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("日期");
            headerRow.createCell(1).setCellValue("营业额");

            int rowNum = 1;
            for (Map<String, Object> map : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(map.get("orderDate").toString());
                row.createCell(1).setCellValue(map.get("turnover").toString());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=business_data.xlsx");
            workbook.write(os);
            os.flush();

            log.info("导出Excel报表成功");
        } catch (IOException e) {
            log.error("导出Excel报表失败：{}", e.getMessage());
            throw new RuntimeException("导出Excel报表失败", e);
        }
    }
}