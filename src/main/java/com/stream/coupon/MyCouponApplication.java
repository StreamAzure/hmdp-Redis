package com.stream.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.stream.coupon.mapper")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class MyCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyCouponApplication.class, args);
    }

}
