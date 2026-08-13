
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
     * 根据三级分类ID查询商品（过渡期保留旧字段）
     * 
     * @param thirdCategoryId 三级分类ID（品牌ID）
     * @return 该分类下的商品列表
     * @deprecated 请使用 findByCategoryId(Long) 替代
     */
    @Deprecated
    List<Product> findByThirdCategoryId(Long thirdCategoryId);

    /**
     * 根据统一分类ID查询商品（新标准）
     * 
     * @param categoryId 统一分类ID（category 表主键）
     * @return 该分类下的商品列表
     */
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

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
     * 使用 MySQL 全文索引搜索商品名称（性能更优）
     * 
     * @param keyword 搜索关键词
     * @return 匹配的商品列表
     */
    List<Product> searchByNameFullText(String keyword);

    /**
     * 根据三级分类ID和价格范围筛选商品（过渡期保留）
     * 使用联合索引 idx_product_category_price 加速查询
     * 
     * @param thirdCategoryId 三级分类ID
     * @param minPrice        最低价格
     * @param maxPrice        最高价格
     * @return 符合条件的商品列表
     * @deprecated 请使用 findByNewCategoryAndPrice 替代
     */
    @Deprecated
    List<Product> findByCategoryAndPrice(@Param("thirdCategoryId") Long thirdCategoryId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice);

    /**
     * 根据统一分类ID和价格范围筛选商品（新标准）
     * 使用联合索引 idx_product_category_price_new(category_id, price) 加速查询
     * 
     * @param categoryId 统一分类ID
     * @param minPrice   最低价格
     * @param maxPrice   最高价格
     * @return 符合条件的商品列表
     */
    List<Product> findByNewCategoryAndPrice(@Param("categoryId") Long categoryId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice);

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

    /**
     * 查询销量 Top N 的热门商品
     * 通过 order_item 表统计每个商品的销售数量，按销量降序返回
     * 
     * @param limit 返回商品数量（整数，大于0）
     * @return 热门商品列表
     */
    List<Product> findHotProducts(@Param("limit") int limit);
}
