package com.stream.coupon.config;

import com.stream.coupon.interceptor.JWTLoginInterceptor;
import com.stream.coupon.interceptor.LoginInterceptor;
import com.stream.coupon.interceptor.RefreshTokenInterceptor;
import com.stream.coupon.utils.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtils jwtUtils;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 登录拦截器，只有访问特定路径时才会被拦截
//        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
//                .excludePathPatterns(
//                        "/shop/**",
//                        "/voucher/**",
//                        "/shop-type/**",
//                        "/upload/**",
//                        "/blog/hot",
//                        "/user/code",
//                        "/user/login"
//                ).order(1);
//        // token刷新拦截器，访问任意页面都刷新token
//        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
        // JWT 登录拦截器
        registry.addInterceptor(new JWTLoginInterceptor(jwtUtils))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/shop/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/user/code",
                        "/user/login"
                ).order(1);
    }
}