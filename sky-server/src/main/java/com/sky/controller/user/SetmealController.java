package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套餐浏览(用户端)
 */
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "C端套餐浏览接口")
public class  SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类ID查询启用的套餐
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询套餐")
    public Result<List<SetmealVO>> list(Long categoryId) {
        log.info("用户端根据分类ID查询套餐，categoryId={}", categoryId);
        List<SetmealVO> setmealVOList = setmealService.listByCategoryId(categoryId);
        return Result.success(setmealVOList);
    }

    /**
     * 根据套餐ID查询套餐包含的菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐ID查询包含的菜品")
    public Result<SetmealVO> dishById(@PathVariable Long id) {
        log.info("用户端根据套餐ID查询菜品，id={}", id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }
}