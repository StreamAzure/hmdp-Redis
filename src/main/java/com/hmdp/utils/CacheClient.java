package com.hmdp.utils;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    // ExecutorService 是 Java 中 java.util.concurrent 包提供的一个接口，用于管理和控制线程池。
    // 它提供了一种更高级别的方式来启动、管理和控制线程的生命周期。
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 设置缓存并设置过期时间
    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    // 设置缓存并设置逻辑过期时间
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入 Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 防缓存穿透
    public <R, ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        // 先到 Redis 中查询
        String json = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(json)){
            log.debug("缓存命中！");
            return JSONUtil.toBean(json, type);
        }
        if (json != null){
            // Redis 对象存在，且值为空，这是防缓存穿透的
            // 返回错误信息
            log.debug("缓存命中！但为空值");
            return null;
        }
        log.debug("缓存未命中！");
        // Redis查不到，再到数据库查询
        // 数据库查询的逻辑方法需要整体作为参数传入
        R r = dbFallback.apply(id);
        if(r == null){
            // 将空值写入Redis，防缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 查到后写入Redis缓存
        this.set(key, r, time, unit);
        return r;
    }

    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, String lockKeyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        // 先到 Redis 中查询
        String json = stringRedisTemplate.opsForValue().get(key);
        // 判断是否存在
        if (StrUtil.isBlank(json)){
            // 不存在，直接返回
            return null;
        }
        // 拿数据，判断是否逻辑过期
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if(expireTime.isAfter(LocalDateTime.now())){
            // 未过期，直接返回
            return r;
        }
        // 已过期，用独立线程进行缓存重建
        String lockKey =  lockKeyPrefix + id;
        boolean isLock = tryLock(lockKey);
        if(isLock){
            // 获取锁成功，开始重建缓存
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                // 查数据库
                R r1 = dbFallback.apply(id);
                try {
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e){
                    throw new RuntimeException(e);
                }finally {
                    unLock(lockKey);
                }
            });
        }
        // 抢不到锁就直接返回过期数据
        return r;
    }

//    public Shop queryWithMutex(Long id) {
//        String key = CACHE_SHOP_KEY + id;
//        Shop shop = null;
//        // 先到 Redis 中查询
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        if(StrUtil.isNotBlank(shopJson)){
//            log.debug("缓存命中！");
//            shop = JSONUtil.toBean(shopJson, Shop.class);
//            return shop;
//        }
//        log.debug("缓存未命中！");
//        // 缓存重建
//        String lockKey = "lock:shop:" + id;
//        try {
//            while (!tryLock(lockKey)) {
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//            // 拿到锁，也要DoubleCheck Redis缓存是否存在
//            Thread.sleep(200); // 模拟缓存重建延时
//            shop = getById(id); // 从数据库中查询
//            if (shop == null) {
//                // 防缓存穿透
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//            // 查到后写入Redis缓存
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            unLock(lockKey);
//        }
//        return shop;
//    }

    private boolean tryLock(String key){
        // 拿锁
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag); // 防止空指针
    }

    private void unLock(String key){
        // 释放锁
        stringRedisTemplate.delete(key);
    }
}
