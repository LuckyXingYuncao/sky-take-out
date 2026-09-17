package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    /**
     * 微信用户登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 开发环境模拟登录：跳过微信服务器，直接用指定 openid 查库/注册。
     * 仅用于本地联调，生产环境不注册该接口。
     * @param openid 模拟的 openid，为空时使用默认值
     * @return
     */
    User devLogin(String openid);
}