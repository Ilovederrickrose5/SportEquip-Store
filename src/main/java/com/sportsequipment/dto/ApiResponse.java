package com.sportsequipment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 统一API响应包装类
 * 
 * @param <T> 响应数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  /**
   * 业务状态码
   */
  private int code;

  /**
   * 响应消息
   */
  private String message;

  /**
   * 响应数据
   */
  private T data;

  /**
   * 时间戳
   */
  private LocalDateTime timestamp;

  /**
   * 请求路径（仅异常时返回）
   */
  private String path;

  /**
   * 错误详情（仅异常时返回）
   */
  private Object errors;

  // ==================== 构造方法 ====================

  public ApiResponse() {
    this.timestamp = LocalDateTime.now();
  }

  public ApiResponse(int code, String message) {
    this.code = code;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }

  public ApiResponse(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  // ==================== 成功响应工厂方法 ====================

  /**
   * 成功响应（无数据）
   */
  public static <T> ApiResponse<T> success() {
    return new ApiResponse<>(200, "操作成功");
  }

  /**
   * 成功响应（带数据）
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(200, "操作成功", data);
  }

  /**
   * 成功响应（自定义消息）
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(200, message, data);
  }

  /**
   * 创建成功响应（201）
   */
  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(201, "创建成功", data);
  }

  // ==================== 失败响应工厂方法 ====================

  /**
   * 失败响应（通用错误）
   */
  public static <T> ApiResponse<T> error(int code, String message) {
    return new ApiResponse<>(code, message);
  }

  /**
   * 失败响应（400 请求错误）
   */
  public static <T> ApiResponse<T> badRequest(String message) {
    return new ApiResponse<>(400, message);
  }

  /**
   * 失败响应（401 未授权）
   */
  public static <T> ApiResponse<T> unauthorized(String message) {
    return new ApiResponse<>(401, message);
  }

  /**
   * 失败响应（403 拒绝访问）
   */
  public static <T> ApiResponse<T> forbidden(String message) {
    return new ApiResponse<>(403, message);
  }

  /**
   * 失败响应（404 资源不存在）
   */
  public static <T> ApiResponse<T> notFound(String message) {
    return new ApiResponse<>(404, message);
  }

  /**
   * 失败响应（500 服务器错误）
   */
  public static <T> ApiResponse<T> serverError(String message) {
    return new ApiResponse<>(500, message);
  }

  // ==================== 链式调用方法 ====================

  public ApiResponse<T> path(String path) {
    this.path = path;
    return this;
  }

  public ApiResponse<T> errors(Object errors) {
    this.errors = errors;
    return this;
  }

  // ==================== Getters and Setters ====================

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Object getErrors() {
    return errors;
  }

  public void setErrors(Object errors) {
    this.errors = errors;
  }
}