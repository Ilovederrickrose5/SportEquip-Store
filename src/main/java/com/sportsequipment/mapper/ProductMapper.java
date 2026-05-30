
package com.sportsequipment.mapper;

import com.sportsequipment.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问接口
 * 
 * 【小白必读】
 * Mapper接口是MyBatis的核心组件，负责定义数据库操作方法。
 * 每个方法对应XML文件中的一条SQL语句，方法名必须与XML中的id一致。
 * 
 * 使用步骤：
 * 1. 在Service层注入这个接口（使用@Autowired）
 * 2. 调用接口方法即可执行对应的SQL
 * 3. MyBatis会自动将结果映射为Java对象
 */
@Mapper // 这是MyBatis的核心注解，告诉Spring这是一个Mapper接口
public interface ProductMapper {

    /**
     * 根据ID查询单个商品
     * 
     * @param id 商品ID（Long类型，非空）
     * @return 商品实体对象（Product），查不到返回null
     */
    Product findById(Long id);

    /**
     * 查询所有商品
     * 
     * @return 商品列表（List<Product>），空表返回空列表
     */
    List<Product> findAll();

    /**
     * 插入新商品
     * 
     * @param product 商品实体对象，包含要插入的所有数据
     *                注意：id字段会由数据库自动生成，无需手动设置
     */
    void insert(Product product);

    /**
     * 更新商品信息
     * 
     * @param product 商品实体对象，必须包含id字段用于定位要更新的记录
     */
    void update(Product product);

    /**
     * 根据ID删除商品
     * 
     * @param id 商品ID
     */
    void deleteById(Long id);

    /**
     * 根据三级分类ID查询商品
     * 
     * @param thirdCategoryId 三级分类ID（品牌ID）
     * @return 该分类下的商品列表
     */
    List<Product> findByThirdCategoryId(Long thirdCategoryId);

    /**
     * 根据商品名称搜索（模糊匹配）
     * 
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    List<Product> searchByName(String keyword);

    /**
     * 综合搜索（名称或描述包含关键词）
     * 
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    List<Product> search(String keyword);

    /**
     * 随机获取指定数量的商品（用于推荐功能）
     * 
     * 【重点】@Param注解说明：
     * 当方法只有一个参数时，XML中可以直接用#{参数名}引用
     * 但为了代码清晰和避免歧义，推荐始终使用@Param注解
     * 
     * @param limit 返回商品数量（整数，大于0）
     * @return 随机商品列表
     */
    List<Product> selectRandomProducts(@Param("limit") int limit);
}
