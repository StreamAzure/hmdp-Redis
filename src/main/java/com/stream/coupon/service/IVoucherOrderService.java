package com.stream.coupon.service;

import com.stream.coupon.dto.Result;
import com.stream.coupon.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    void createVoucherOrder(VoucherOrder voucherOrder);

    Result commonVoucher(Long voucherId, int buyNumber);

    Result seckillVoucher(Long voucherId);

    Result limitVoucher(Long voucherId, int buyNumber);
}
