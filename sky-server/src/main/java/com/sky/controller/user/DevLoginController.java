package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 开发环境模拟登录接口。
 *
 * <p>背景：真实登录需要小程序把 wx.login() 拿到的 code 交给后端，
 * 后端再请求微信服务器换取 openid。本地开发时若没有可用的 AppID，
 * 这一步必然失败，导致整个用户端功能无法联调。</p>
 *
 * <p>该接口跳过微信服务器，直接用约定好的 openid 查库/自动注册并签发 JWT，
 * 让前端可以在纯本地环境跑通全流程。</p>
 *
 * <p><b>安全约束：</b>类上标注了 {@code @Profile("dev")}，
 * 只有 {@code spring.profiles.active=dev} 时才会被注册成 Bean，
 * 生产环境（prod）下这个接口根本不存在，因此不会形成越权后门。</p>
 */
@RestController
@Profile("dev")
@RequestMapping("/user/user")
@Slf4j
@Api(tags = "开发环境模拟登录接口")
public class DevLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 模拟登录
     *
     * @param body 可选，形如 {"openid":"dev_openid_001"}，用于模拟不同用户
     * @return 与真实登录完全一致的 UserLoginVO（含 token）
     */
    @PostMapping("/dev-login")
    @ApiOperation("开发环境模拟登录（跳过微信服务器）")
    public Result<UserLoginVO> devLogin(@RequestBody(required = false) Map<String, String> body) {
        String openid = body == null ? null : body.get("openid");
        log.info("【开发登录】模拟登录请求，openid={}", openid);

        User user = userService.devLogin(openid);

        // 与 UserController#login 保持一致：签发用户端 JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        log.info("【开发登录】成功，userId={}", user.getId());
        return Result.success(userLoginVO);
    }
}
