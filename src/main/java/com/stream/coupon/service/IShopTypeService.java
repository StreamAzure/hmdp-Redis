package com.stream.coupon.service;

import com.stream.coupon.dto.Result;
import com.stream.coupon.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopTypeService extends IService<ShopType> {

    Result queryTypeList();
}
