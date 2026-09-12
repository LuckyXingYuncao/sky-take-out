package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @CacheEvict(value = {"user:category", "user:dish", "user:setmeal"}, allEntries = true)
    public void save(CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.ENABLE);
        categoryMapper.insert(category);
        log.info("新增分类成功：{}", category.getName());
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类分页查询：{}", categoryPageQueryDTO);
        int page = categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize();
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        PageHelper.startPage(page, pageSize);
        List<Category> categoryList = categoryMapper.pageQuery(categoryPageQueryDTO);
        Page<Category> p = (Page<Category>) categoryList;
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    @CacheEvict(value = {"user:category", "user:dish", "user:setmeal"}, allEntries = true)
    public void startOrStop(Integer status, Long id) {
        log.info("启用/禁用分类：id={}, status={}", id, status);
        Category category = Category.builder()
                .status(status)
                .id(id)
                .build();
        categoryMapper.update(category);
    }

    @Override
    public Category getById(Long id) {
        log.info("根据ID查询分类：id={}", id);
        Category category = categoryMapper.getById(id);
        return category;
    }

    @Override
    @CacheEvict(value = {"user:category", "user:dish", "user:setmeal"}, allEntries = true)
    public void update(CategoryDTO categoryDTO) {
        log.info("修改分类信息：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.update(category);
    }

    @Override
    @CacheEvict(value = {"user:category", "user:dish", "user:setmeal"}, allEntries = true)
    public void deleteById(Long id) {
        log.info("删除分类：id={}", id);
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> getByType(Integer type) {
        log.info("根据类型查询分类：type={}", type);
        List<Category> categoryList = categoryMapper.getByType(type);
        return categoryList;
    }

    @Override
    @Cacheable(value = "user:category", key = "#type != null ? #type : 'all'", unless = "#result.isEmpty()")
    public List<Category> listByType(Integer type) {
        log.info("用户端根据类型查询启用的分类：type={}", type);
        List<Category> categoryList = categoryMapper.listByType(type);
        return categoryList;
    }
}