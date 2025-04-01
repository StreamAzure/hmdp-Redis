package com.stream.coupon.common;

public enum ChainBizMarkEnum {
    /**
     * 创建优惠券验证参数是否正确责任链流程
     */
    SHOP_CREATE_COUPON_KEY;

    @Override
    public String toString() {
        return this.name();
    }
}
