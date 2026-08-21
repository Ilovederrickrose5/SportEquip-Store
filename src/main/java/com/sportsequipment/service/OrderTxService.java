package com.sportsequipment.service;

import com.sportsequipment.dto.OrderDTO;
import com.sportsequipment.dto.OrderItemDTO;
import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单事务服务：只负责"拿齐分布式锁之后"的纯事务内写操作。
 * 独立成一个 Service 的原因：@Transactional 必须通过代理对象调用才能生效，
 * 外层 OrderServiceImpl 先拿锁，再通过 Spring 代理调这里的方法，
 * 保证"事务先提交 → 锁后释放"的正确顺序。
 */
public interface OrderTxService {

  /**
   * 事务内执行：二次库存检查 → 生成订单主表 → 扣库存 → 生成订单项。
   * 调用方必须确保所有商品的分布式锁已拿到！
   *
   * @param orderDTO    前端入参
   * @param dtoItems    订单项列表
   * @param totalAmount 已计算好的总金额
   * @param user        当前用户
   * @return 持久化后的 Order 实体（含自增 id、订单项关联）
   */
  Order doCreateOrderInTx(OrderDTO orderDTO, List<OrderItemDTO> dtoItems,
      BigDecimal totalAmount, User user);
}
