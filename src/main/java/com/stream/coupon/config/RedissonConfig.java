package com.stream.coupon.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        // master 1
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

//    @Bean
//    public RedissonClient redissonClient1(){
//        // master 2
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://localhost:6380");
//        return Redisson.create(config);
//    }
//    @Bean
//    public RedissonClient redissonClient2(){
//        // master 3
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://localhost:6381");
//        return Redisson.create(config);
//    }
}
