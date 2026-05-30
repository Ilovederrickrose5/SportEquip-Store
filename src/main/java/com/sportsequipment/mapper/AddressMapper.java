
package com.sportsequipment.mapper;

import com.sportsequipment.entity.Address;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收货地址数据访问接口
 * 
 * 【小白必读】
 * 地址表（address）用于存储用户的收货地址信息。
 * 用户可以有多个地址，但只能设置一个默认地址。
 */
@Mapper
public interface AddressMapper {

    /**
     * 根据ID查询地址
     * 
     * @param id 地址ID
     * @return 地址实体
     */
    Address findById(Long id);

    /**
     * 查询所有地址（管理员使用）
     * 
     * @return 地址列表
     */
    List<Address> findAll();

    /**
     * 添加新地址
     * 
     * @param address 地址实体，包含省、市、区、详细地址、收货人、电话等
     */
    void insert(Address address);

    /**
     * 更新地址信息
     * 
     * @param address 地址实体，必须包含id
     */
    void update(Address address);

    /**
     * 根据ID删除地址
     * 
     * @param id 地址ID
     */
    void deleteById(Long id);

    /**
     * 根据用户ID查询所有地址
     * 
     * @param userId 用户ID
     * @return 用户的所有收货地址
     */
    List<Address> findByUserId(Long userId);

    /**
     * 查询用户的默认地址
     * 
     * @param userId 用户ID
     * @return 默认地址，如果没有设置默认地址则返回null
     */
    Address findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * 删除用户的所有地址（用户注销时使用）
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
}
