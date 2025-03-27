package com.hmdp.controller;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 商品下单 Controller：普通商品下单、限购商品下单、秒杀商品下单
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
