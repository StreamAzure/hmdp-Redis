package com.stream.coupon.service.filter;

import com.stream.coupon.common.ChainBizMarkEnum;
import com.stream.coupon.common.exception.ClientException;
import com.stream.coupon.entity.Voucher;
import org.springframework.stereotype.Component;

/**
 * 商家创建优惠券必填字段校验
 * 优惠券必填字段：shopId, title, sub_title, rules, pay_value, actual_value, type（0普通，1限购，2秒杀）, status（0上架，1下架）
 */

@Component
public class CouponCreateParamNotNullChainFilter implements ShopAbstractChainHandler<Voucher>{
    @Override
    public void handler(Voucher requestParam) {
        if (requestParam.getShopId() == null) {
            throw new ClientException("商家id不能为空");
        }
        if (requestParam.getTitle() == null) {
            throw new ClientException("优惠券标题不能为空");
        }
        if (requestParam.getSubTitle() == null) {
            throw new ClientException("优惠券副标题不能为空");
        }
        if (requestParam.getRules() == null) {
            throw new ClientException("优惠券规则不能为空");
        }
        if (requestParam.getPayValue() == null) {
            throw new ClientException("优惠券支付金额不能为空");
        }
        if (requestParam.getActualValue() == null) {
            throw new ClientException("优惠券实际金额不能为空");
        }
        if (requestParam.getType() == null) {
            throw new ClientException("优惠券类型不能为空");
        }
        if (requestParam.getStatus() == null) {
            throw new ClientException("优惠券状态不能为空");
        }
    }

    @Override
    public String getBizMark() {
        return ChainBizMarkEnum.SHOP_CREATE_COUPON_KEY.name();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
