package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    User getByOpenid(String openid);

    /**
     * 插入用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据ID查询用户
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 更新用户信息
     * @param user
     */
    void update(User user);
}