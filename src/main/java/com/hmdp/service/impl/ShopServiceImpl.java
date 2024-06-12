package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private boolean tryLock(String key){
        // 拿锁
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag); // 防止空指针
    }

    private void unLock(String key){
        // 释放锁
        stringRedisTemplate.delete(key);
    }

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        Shop shop = queryWithPassThrough(id);
        if (shop == null){
            return Result.fail("店铺不存在！");
        }
        return Result.ok(shop);
    }

    public Shop queryWithMutex(Long id) {
        String key = "cache:shop:" + id;
        Shop shop = null;
        // 先到 Redis 中查询
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(shopJson)){
            log.debug("缓存命中！");
            shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        log.debug("缓存未命中！");
        // 缓存重建
        String lockKey = "lock:shop:" + id;
        try {
            while (!tryLock(lockKey)) {
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            // 拿到锁，也要DoubleCheck Redis缓存是否存在
            Thread.sleep(200); // 模拟缓存重建延时
            shop = getById(id); // 从数据库中查询
            if (shop == null) {
                // 防缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 查到后写入Redis缓存
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unLock(lockKey);
        }
        return shop;
    }

    public Shop queryWithPassThrough(Long id){
        String key = "cache:shop:" + id;
        // 先到 Redis 中查询
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(shopJson)){
            log.debug("缓存命中！");
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        log.debug("缓存未命中！");
        // Redis查不到，再到数据库查询
        Shop shop = getById(id);
        if(shop == null){
            return null;
        }
        // 查到后写入Redis缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    @Override
    public Result update(Shop shop) {
        Long id = shop.getId();
        if(id == null){
            return Result.fail("店铺ID不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }
}
