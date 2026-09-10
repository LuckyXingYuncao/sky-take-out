package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类查询(用户端)
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
@Api(tags = "C端分类查询接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据类型查询启用的分类
     * @param type 分类类型 1=菜品分类 2=套餐分类
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        log.info("用户端查询分类列表，type={}", type);
        List<Category> categoryList = categoryService.listByType(type);
        log.info("查询结果数量：{}", categoryList.size());
        return Result.success(categoryList);
    }
}