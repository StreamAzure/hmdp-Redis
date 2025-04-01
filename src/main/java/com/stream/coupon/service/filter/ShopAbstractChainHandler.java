package com.stream.coupon.service.filter;

import org.springframework.core.Ordered;

/**
 * 商家服务抽象责任链组件
 */
public interface ShopAbstractChainHandler<T> extends Ordered {

    /**
     * 处理请求
     *
     * @param requestParam 请求参数
     */
    void handler(T requestParam);

    /**
     * 获取当前责任链的业务标识
     *
     * @return 业务标识
     */
    String getBizMark();
}
