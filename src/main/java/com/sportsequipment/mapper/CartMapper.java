
package com.sportsequipment.mapper;

import com.sportsequipment.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 购物车数据访问接口
 * 
 * 【小白必读】
 * 购物车表（cart）用于存储用户的购物车信息。
 * 购物车项（cart_item）表存储具体的商品条目。
 * 一个用户对应一个购物车，一个购物车可以有多个购物车项。
 */
@Mapper
public interface CartMapper {

    /**
     * 根据ID查询购物车
     * 
     * @param id 购物车ID
     * @return 购物车实体
     */
    Cart findById(Long id);

    /**
     * 查询所有购物车（管理员使用）
     * 
     * @return 购物车列表
     */
    List<Cart> findAll();

    /**
     * 创建新购物车
     * 
     * @param cart 购物车实体，包含用户ID
     */
    void insert(Cart cart);

    /**
     * 更新购物车信息
     * 
     * @param cart 购物车实体，必须包含id
     */
    void update(Cart cart);

    /**
     * 根据ID删除购物车
     * 
     * @param id 购物车ID
     */
    void deleteById(Long id);

    /**
     * 根据用户ID查询购物车
     * 
     * @param userId 用户ID
     * @return 用户的购物车（一个用户只有一个购物车）
     */
    Cart findByUserId(Long userId);
}
