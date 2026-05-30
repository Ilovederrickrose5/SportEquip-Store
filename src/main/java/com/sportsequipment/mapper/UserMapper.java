
package com.sportsequipment.mapper;

import com.sportsequipment.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户数据访问接口
 * 
 * 【小白必读】
 * 这个接口定义了对用户表（user）的所有数据库操作。
 * 每个方法对应XML文件中的一条SQL语句。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户实体
     */
    User findById(Long id);

    /**
     * 查询所有用户
     * 
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 插入新用户（注册）
     * 
     * @param user 用户实体，包含用户名、密码、邮箱等信息
     */
    void insert(User user);

    /**
     * 更新用户信息
     * 
     * @param user 用户实体，必须包含id
     */
    void update(User user);

    /**
     * 根据ID删除用户
     * 
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 根据用户名查询用户（用于登录验证）
     * 
     * @param username 用户名
     * @return 用户实体，登录时用于验证密码
     */
    User findByUsername(String username);

    /**
     * 检查用户名是否已存在（注册校验）
     * 
     * @param username 用户名
     * @return 1表示存在，0表示不存在
     */
    Integer existsByUsername(String username);

    /**
     * 检查邮箱是否已被使用（注册校验）
     * 
     * @param email 邮箱地址
     * @return 1表示存在，0表示不存在
     */
    Integer existsByEmail(String email);
}
