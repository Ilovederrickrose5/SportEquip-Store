package com.sportsequipment.dto;

/**
 * API 错误码枚举
 * 
 * 错误码规则：
 * - 1xxx：通用错误
 * - 2xxx：认证相关错误
 * - 3xxx：商品相关错误
 * - 4xxx：订单相关错误
 * - 5xxx：购物车相关错误
 * - 6xxx：用户相关错误
 */
public enum ApiErrorCode {

    // ==================== 通用错误（1xxx）====================
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SYSTEM_BUSY(1001, "系统繁忙，请稍后再试"),

    // ==================== 认证相关（2xxx）====================
    AUTH_TOKEN_INVALID(2001, "Token无效或已过期"),
    AUTH_TOKEN_EXPIRED(2002, "Token已过期"),
    AUTH_USER_NOT_FOUND(2003, "用户不存在"),
    AUTH_PASSWORD_ERROR(2004, "密码错误"),
    AUTH_USER_EXISTS(2005, "用户已存在"),
    AUTH_EMAIL_EXISTS(2006, "邮箱已被注册"),
    AUTH_USERNAME_EXISTS(2007, "用户名已存在"),

    // ==================== 商品相关（3xxx）====================
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_STOCK_INSUFFICIENT(3002, "库存不足"),
    PRODUCT_DELETE_HAS_ORDERS(3003, "该商品已有订单，无法删除"),

    // ==================== 订单相关（4xxx）====================
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_INVALID(4002, "订单状态无效"),
    ORDER_CANNOT_CANCEL(4003, "订单已支付，无法取消"),
    ORDER_CANNOT_REFUND(4004, "订单状态不支持退款"),

    // ==================== 购物车相关（5xxx）====================
    CART_NOT_FOUND(5001, "购物车不存在"),
    CART_ITEM_NOT_FOUND(5002, "购物车商品不存在"),

    // ==================== 用户相关（6xxx）====================
    USER_NOT_FOUND(6001, "用户不存在"),
    USER_PASSWORD_NOT_MATCH(6002, "原密码不正确"),
    USER_ROLE_INVALID(6003, "角色无效"),

    // ==================== 分类相关（7xxx）====================
    CATEGORY_NOT_FOUND(7001, "分类不存在"),
    CATEGORY_HAS_CHILDREN(7002, "该分类下有子分类，无法删除"),
    CATEGORY_HAS_PRODUCTS(7003, "该分类下有商品，无法删除"),

    // ==================== 文件相关（8xxx）====================
    FILE_UPLOAD_ERROR(8001, "文件上传失败"),
    FILE_SIZE_EXCEEDED(8002, "文件大小超出限制"),
    FILE_TYPE_INVALID(8003, "文件类型不允许"),
    FILE_NOT_FOUND(8004, "文件不存在");

    private final int code;
    private final String message;

    ApiErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取错误枚举
     */
    public static ApiErrorCode fromCode(int code) {
        for (ApiErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }

    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.success();
    }

    /**
     * 创建成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * 创建失败响应
     */
    public ApiResponse<Void> toResponse() {
        ApiResponse<Void> response = new ApiResponse<>(this.code, this.message);
        return response;
    }

    /**
     * 创建失败响应（自定义消息）
     */
    public ApiResponse<Void> toResponse(String customMessage) {
        ApiResponse<Void> response = new ApiResponse<>(this.code, customMessage);
        return response;
    }
}