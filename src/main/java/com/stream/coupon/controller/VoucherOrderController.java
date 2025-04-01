package com.stream.coupon.controller;
import com.stream.coupon.dto.Result;
import com.stream.coupon.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 优惠券购买 Controller：普通优惠券下单、限购优惠券下单、秒杀优惠券下单
 */
@RestController
@Slf4j
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("/common/{id}")
    public Result commonVoucher(@PathVariable("id") Long voucherId) {
        log.info("普通下单");
        int buyNumber = 1; // 购买数量
        return voucherOrderService.commonVoucher(voucherId, buyNumber);
    }

    public Result limitVoucher(@PathVariable("id") Long voucherId) {
        log.info("限购下单");
        int buyNumber = 1;
        return voucherOrderService.limitVoucher(voucherId, buyNumber);
    }

    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        log.debug("秒杀下单");
        return voucherOrderService.seckillVoucher(voucherId);
    }
}
