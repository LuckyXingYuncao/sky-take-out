package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 微信用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String code = userLoginDTO.getCode();
        log.info("微信用户登录，code：{}", code);

        //调用微信接口，获取openid
        String json = restTemplate.getForObject(WX_LOGIN_URL, String.class,
                weChatProperties.getAppid(),
                weChatProperties.getSecret(),
                code);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        //判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if (openid == null) {
            log.error("微信登录失败，获取openid为空，返回数据：{}", json);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        log.info("微信登录成功，openid：{}", openid);

        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        //如果是新用户，自动注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            log.info("新用户注册成功，id：{}", user.getId());
        }

        return user;
    }
}