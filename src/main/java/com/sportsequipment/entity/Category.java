package com.sportsequipment.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 统一分类实体类（企业标准邻接表结构）
 * 替代原有 main_category / sub_category / third_category 三张物理表
 * 通过 parentId 自关联 + level 冗余字段实现任意级分类
 *
 * @author sports-equipment-team
 */
@Getter
@Setter
public class Category {

  private Long id;

  @NotBlank(message = "分类名称不能为空")
  @Size(max = 64, message = "分类名称不能超过64个字符")
  private String name;

  /**
   * 父分类ID，NULL表示顶级分类
   * 自关联本表主键id
   */
  private Long parentId;

  /**
   * 分类层级
   * 1 = 一级分类
   * 2 = 二级分类
   * 3 = 三级分类
   * 4+ 可按需无限扩展
   */
  @NotNull(message = "分类层级不能为空")
  private Integer level;

  /**
   * 同层展示顺序，数值越小越靠前
   * 运营后台可拖拽调整排序
   */
  private Integer sortOrder = 0;

  /**
   * 状态：1 = 启用，0 = 禁用（软启停，硬删除走deleted字段）
   */
  private Integer status = 1;

  /**
   * 分类描述（兼容旧 Main/Sub/Third Category 的 description 字段）
   */
  @Size(max = 512, message = "分类描述长度超过限制")
  private String description;

  /**
   * 分类图标URL（一般一级分类才有）
   */
  @Size(max = 255, message = "分类图标URL不能超过255个字符")
  private String iconUrl;

  /**
   * 冗余：完整分类ID路径，逗号拼接
   * 例：一级id=1，二级id=10005，三级id=100023 → "1,10005,100023"
   * 用途：快速回溯祖先链、面包屑加速查询
   */
  @Size(max = 512, message = "分类路径长度超过限制")
  private String path;

  /**
   * 冗余：完整分类名称路径，斜杠分隔
   * 例："球类运动 / 篮球 / 比赛篮球"
   * 用途：直接返回给前端面包屑展示，免查多次
   */
  @Size(max = 512, message = "分类名称路径长度超过限制")
  private String pathName;

  /**
   * SEO关键词（企业项目通用预留字段）
   */
  @Size(max = 255, message = "SEO关键词长度超过限制")
  private String seoKeywords;

  /**
   * SEO描述（企业项目通用预留字段）
   */
  @Size(max = 512, message = "SEO描述长度超过限制")
  private String seoDescription;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  /**
   * 软删除标记：0 = 正常，1 = 已删除
   * 删除分类只改这个标记，历史商品数据不受影响
   */
  private Integer deleted = 0;

  // -------------------- 以下为非数据库字段，仅用于业务组装 --------------------

  /**
   * 父分类对象（MyBatis关联查询时映射）
   */
  private Category parent;

  /**
   * 直接子分类列表（递归组装分类树用）
   */
  private List<Category> children = new ArrayList<>();
}
