package com.stream.coupon.service;

import com.stream.coupon.dto.Result;
import com.stream.coupon.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopService extends IService<Shop> {


    Result queryById(Long id);

    Result update(Shop shop);
}
