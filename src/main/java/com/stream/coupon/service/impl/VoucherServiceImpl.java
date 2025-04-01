package com.stream.coupon.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stream.coupon.common.ChainBizMarkEnum;
import com.stream.coupon.common.VoucherTypeEnum;
import com.stream.coupon.dto.Result;
import com.stream.coupon.entity.Voucher;
import com.stream.coupon.mapper.VoucherMapper;
import com.stream.coupon.entity.SeckillVoucher;
import com.stream.coupon.service.ISeckillVoucherService;
import com.stream.coupon.service.IVoucherService;
import com.stream.coupon.service.filter.ShopChainContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.stream.coupon.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * 商品增删改查服务实现类
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 优惠券创建参数校验责任链上下文
     */
    @Resource
    private ShopChainContext shopChainContext;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    /**
     * 商家添加优惠券
     * @param voucher
     */
    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 优惠券创建参数校验
        shopChainContext.handler(ChainBizMarkEnum.SHOP_CREATE_COUPON_KEY.name(), voucher);

        // 保存优惠券
        save(voucher);

        if (Objects.equals(voucher.getType(), VoucherTypeEnum.SECKILL.getCode())) {
            // 保存秒杀券库存信息到 seckill_voucher 表
            SeckillVoucher seckillVoucher = new SeckillVoucher();
            seckillVoucher.setVoucherId(voucher.getId());
            seckillVoucher.setStock(voucher.getStock());
            seckillVoucher.setBeginTime(voucher.getBeginTime());
            seckillVoucher.setEndTime(voucher.getEndTime());
            seckillVoucherService.save(seckillVoucher);
            // 保存秒杀优惠券到Redis，预热
            stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
        }
    }
}
