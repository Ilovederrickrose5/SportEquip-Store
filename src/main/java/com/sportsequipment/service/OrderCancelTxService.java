package com.sportsequipment.service;

import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;

import java.util.List;

/**
 * 订单取消事务服务：只负责"拿齐分布式锁之后"的纯事务内写操作。
 * 独立成一个 Service 的原因：@Transactional 必须通过代理对象调用才能生效，
 * 外层 OrderCancelService 先拿锁，再通过 Spring 代理调这里的方法，
 * 保证"事务先提交 → 锁后释放"的正确顺序（与 OrderTxService 同一模式）。
 */
public interface OrderCancelTxService {

    /**
     * 事务内执行：归还所有订单项库存 + 订单状态置 CANCELLED。
     * 调用方必须确保所有涉及商品的分布式锁已拿齐！
     *
     * @param order  已校验为 PENDING 的订单（方法内会修改其 status 并 update）
     * @param items  订单项列表（用于按 productId 归还库存）
     */
    void restoreStockAndMarkCancelledInTx(Order order, List<OrderItem> items);
}
