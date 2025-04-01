package com.stream.coupon.common;

public enum VoucherTypeEnum {
    /**
     * 0：普通优惠券
     * 1：限购优惠券
     * 2：秒杀优惠券
     */
    NORMAL(0, "普通优惠券"),
    LIMIT(1, "限购优惠券"),
    SECKILL(2, "秒杀优惠券");

    private Integer code;

    VoucherTypeEnum(int i, String name) {
        this.code = i;
    }

    public Integer getCode() {
        return code;
    }
}
