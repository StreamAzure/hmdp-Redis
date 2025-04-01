package com.stream.coupon.service.impl;

import com.stream.coupon.entity.SeckillVoucher;
import com.stream.coupon.mapper.SeckillVoucherMapper;
import com.stream.coupon.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 秒杀优惠券库存表，与优惠券是一对一关系
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
