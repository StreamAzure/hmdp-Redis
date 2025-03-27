package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        String key = "cache:shoptype";
        List<String> jsonTypeList = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<ShopType> shopTypeList = new ArrayList<>();
        if(jsonTypeList != null && !jsonTypeList.isEmpty()){
            for(String jsonType : jsonTypeList){
                ShopType shopType = JSONUtil.toBean(jsonType, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }
        // 查数据库
        shopTypeList = query().orderByAsc("sort").list();
        // 写入 Redis
        for(ShopType shopType : shopTypeList){
            String jsonShopType = JSONUtil.toJsonStr(shopType);
            stringRedisTemplate.opsForList().rightPush(key, jsonShopType);
        }
        return Result.ok(shopTypeList);
    }
}
