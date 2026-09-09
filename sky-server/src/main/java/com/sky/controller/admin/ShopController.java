package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺操作
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺操作接口")
public class ShopController {

    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取营业状态
     * @return 营业状态 1:营业中 0:打烊
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus() {
        log.info("获取营业状态");
        String status = stringRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        Integer shopStatus = status != null ? Integer.parseInt(status) : 0;
        return Result.success(shopStatus);
    }

    /**
     * 设置营业状态
     * @param status 1:营业中 0:打烊
     * @return 成功提示
     */
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result<String> setStatus(@PathVariable Integer status) {
        log.info("设置营业状态：{}", status == 1 ? "营业中" : "打烊");
        stringRedisTemplate.opsForValue().set(SHOP_STATUS_KEY, String.valueOf(status));
        return Result.success();
    }
}