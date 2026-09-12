package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        ShoppingCart existing = shoppingCartMapper.getByCondition(shoppingCart);

        if (existing != null) {
            existing.setNumber(existing.getNumber() + 1);
            shoppingCartMapper.updateNumberById(existing);
            log.info("购物车已有该商品，数量+1：{}", existing);
        } else {
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();

            if (dishId != null) {
                Dish dish = dishMapper.getById(dishId);
                if (dish == null) {
                    throw new ShoppingCartBusinessException("菜品不存在");
                }
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else if (setmealId != null) {
                Setmeal setmeal = setmealMapper.getById(setmealId);
                if (setmeal == null) {
                    throw new ShoppingCartBusinessException("套餐不存在");
                }
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            } else {
                throw new ShoppingCartBusinessException("菜品ID和套餐ID不能同时为空");
            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
            log.info("购物车新增商品：{}", shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> listShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.listByUserId(userId);
        log.info("查询购物车，用户ID={}，共{}条", userId, list.size());
        return list;
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
        log.info("清空购物车，用户ID={}", userId);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        ShoppingCart existing = shoppingCartMapper.getByCondition(shoppingCart);

        if (existing == null) {
            throw new ShoppingCartBusinessException("购物车中不存在该商品");
        }

        Integer number = existing.getNumber();
        if (number > 1) {
            existing.setNumber(number - 1);
            shoppingCartMapper.updateNumberById(existing);
            log.info("购物车商品数量-1：{}", existing);
        } else {
            shoppingCartMapper.deleteById(existing.getId());
            log.info("购物车删除商品：{}", existing);
        }
    }
}