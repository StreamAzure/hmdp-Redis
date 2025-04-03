package com.stream.coupon.interceptor;

import cn.hutool.core.util.StrUtil;
import com.stream.coupon.dto.UserDTO;
import com.stream.coupon.utils.JwtUtils;
import com.stream.coupon.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class JWTLoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JWTLoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 在这里进行JWT验证
        String token = request.getHeader("Authorization");
        log.info("JWTLoginInterceptor token: {}", token);
        // 为空，返回 401
        if (token == null || StrUtil.isBlank(token)) {
            log.warn("JWT token is missing");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
        if (!jwtUtils.validateJwtToken(token)) {
            log.warn("Invalid JWT token: {}", token);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
        UserDTO userDTO = jwtUtils.getUserFromJwtToken(token);
        // 保存信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        return true;
    }
}
