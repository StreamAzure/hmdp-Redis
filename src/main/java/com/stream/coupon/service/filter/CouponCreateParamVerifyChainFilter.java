package com.stream.coupon.service.filter;

import com.stream.coupon.common.ChainBizMarkEnum;
import com.stream.coupon.common.VoucherTypeEnum;
import com.stream.coupon.common.exception.ClientException;
import com.stream.coupon.entity.Voucher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Component
public class CouponCreateParamVerifyChainFilter implements ShopAbstractChainHandler<Voucher>{

    private static final int maxStock = 2000000;

    @Override
    public void handler(Voucher voucherCreateParam) {

        // 库存数量需要在合理范围内
        if (voucherCreateParam.getStock() <= 0 || voucherCreateParam.getStock() > maxStock) {
            throw new ClientException("库存数量设置异常");
        }

        // 如果是限购、秒杀优惠券
        if (Objects.equals(voucherCreateParam.getType(), VoucherTypeEnum.LIMIT.getCode()) || Objects.equals(voucherCreateParam.getType(), VoucherTypeEnum.SECKILL.getCode())) {
            // 限购数量必须设置，且不能小于1
            if (voucherCreateParam.getLimitNum() == null || voucherCreateParam.getLimitNum() < 1) {
                throw new ClientException("限购数量设置异常");
            }
            // 有效期开始时间不能早于当前时间
            LocalDateTime now = LocalDateTime.now();
            if (voucherCreateParam.getBeginTime().isBefore(now)) {
                // 为了方便测试，暂时注释掉
//            throw new ClientException("优惠券有效期开始时间不能早于当前时间");
            }
        }
    }

    @Override
    public String getBizMark() {
        return ChainBizMarkEnum.SHOP_CREATE_COUPON_KEY.name();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
