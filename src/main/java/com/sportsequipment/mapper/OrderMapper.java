
package com.sportsequipment.mapper;

import com.sportsequipment.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单数据访问接口
 * 
 * 【小白必读】
 * 订单表是电商系统的核心表之一，用于存储用户的购买记录。
 * 订单状态流转：待支付 -> 已支付 -> 已发货 -> 已完成 -> 已取消
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据ID查询订单详情
     * 
     * @param id 订单ID
     * @return 订单实体，包含订单基本信息
     */
    Order findById(Long id);

    /**
     * 查询所有订单（管理员使用）
     * 
     * @return 订单列表
     */
    List<Order> findAll();

    /**
     * 创建新订单
     * 
     * @param order 订单实体，包含用户ID、收货地址、总金额等
     */
    void insert(Order order);

    /**
     * 更新订单信息（如状态变更）
     * 
     * @param order 订单实体，必须包含id
     */
    void update(Order order);

    /**
     * 根据ID删除订单
     * 
     * @param id 订单ID
     */
    void deleteById(Long id);

    /**
     * 根据用户ID查询订单列表（用户个人中心使用）
     * 
     * @param userId 用户ID
     * @return 该用户的所有订单
     */
    List<Order> findByUserId(Long userId);
}
