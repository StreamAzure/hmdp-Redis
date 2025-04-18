package com.stream.coupon;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class LockTest {
    @Resource
    private RedissonClient redissonClient;

    private RLock lock;

    @BeforeEach
    void setUp(){
        lock = redissonClient.getLock("order");
    }

    /**
     * 在一个线程里两次获取锁
     * 锁的可重入测试
     */
    @Test
    void method1() throws InterruptedException {
        boolean isLock = lock.tryLock(1L, TimeUnit.SECONDS);
        if(!isLock){
            log.error("获取锁失败，1");
            return;
        }
        try{
            log.info("获取锁成功，1");
            method2();
            log.info("开始执行业务……1");
        }finally {
            log.info("释放锁，1");
            lock.unlock();
        }
    }
    void method2(){
        boolean isLock = lock.tryLock();
        if(!isLock){
            log.error("获取锁失败，2");
            return;
        }
        try{
            log.info("获取锁成功，2");
        }finally {
            log.info("释放锁，2");
            lock.unlock();
        }
    }

}
