
package com.sportsequipment.mapper;

import com.sportsequipment.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 根据ID查询订单详情（包含订单项）
     * 
     * @param id 订单ID
     * @return 订单实体，包含订单基本信息和订单项
     */
    Order findById(Long id);

    /**
     * 查询所有订单（管理员使用，包含订单项）
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
     * 根据用户ID查询订单列表（包含订单项）
     * 
     * @param userId 用户ID
     * @return 该用户的所有订单
     */
    List<Order> findByUserId(Long userId);

    /**
     * 分页查询用户订单（不含订单项，用于列表页）
     * 
     * @param userId 用户ID
     * @param status 订单状态（可选，为null时查询所有状态）
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 订单列表
     */
    List<Order> findByUserIdWithPagination(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 分页查询所有订单（不含订单项，用于管理员列表页）
     * 
     * @param status 订单状态（可选，为null时查询所有状态）
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 订单列表
     */
    List<Order> findAllWithPagination(
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计用户订单数量
     * 
     * @param userId 用户ID
     * @param status 订单状态（可选，为null时统计所有状态）
     * @return 订单数量
     */
    int countByUserId(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 统计所有订单数量
     * 
     * @param status 订单状态（可选，为null时统计所有状态）
     * @return 订单数量
     */
    int countAll(@Param("status") String status);
}
