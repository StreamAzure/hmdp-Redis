package com.stream.coupon.service.filter;

import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 商家服务责任链上下文容器
 */
public class ShopChainContext<T> implements ApplicationContextAware, CommandLineRunner {

    /**
     * 应用上下文，我们这里通过 Spring IOC 获取 Bean 实例
     */
    private ApplicationContext applicationContext;


    /**
     * 保存责任链上所有 Handler 的实现类
     */
    private final Map<String, List<ShopAbstractChainHandler>> abstractChainHandlerContainer = new HashMap<>();

    /**
     * 根据 mark 标识从责任链容器中获取一组责任链实现 Bean 集合，并 for 循环依次执行请求校验
     */
    public void handler(String mark, T requestParam) {
        // 根据 mark 标识从责任链容器中获取一组责任链实现 Bean 集合
        List<ShopAbstractChainHandler> abstractChainHandlers = abstractChainHandlerContainer.get(mark);
        if (CollectionUtils.isEmpty(abstractChainHandlers)) {
            throw new RuntimeException(String.format("[%s] Chain of Responsibility ID is undefined.", mark));
        }
        abstractChainHandlers.forEach(each -> each.handler(requestParam));
    }


    /**
     * 实现了 CommandLineRunner 接口，重写 run 方法，Spring Boot 启动时执行
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        // 从 Spring IOC 容器中获取指定接口 Spring Bean 集合
        Map<String, ShopAbstractChainHandler> chainFilterMap = applicationContext.getBeansOfType(ShopAbstractChainHandler.class);
        chainFilterMap.forEach((beanName, bean) -> {
            // 判断 Mark 是否已经存在抽象责任链容器中，如果已经存在直接向集合新增；如果不存在，创建 Mark 和对应的集合
            List<ShopAbstractChainHandler> abstractChainHandlers = abstractChainHandlerContainer.getOrDefault(bean.mark(), new ArrayList<>());
            abstractChainHandlers.add(bean);
            abstractChainHandlerContainer.put(bean.getBizMark(), abstractChainHandlers);
        });
        abstractChainHandlerContainer.forEach((mark, unsortedChainHandlers) -> {
            // 对每个 Mark 对应的责任链实现类集合进行排序，优先级小的在前
            unsortedChainHandlers.sort(Comparator.comparing(Ordered::getOrder));
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
