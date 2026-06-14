package com.sportsequipment.dto;

import java.util.List;

/**
 * 分页响应包装类
 * 
 * @param <T> 数据类型
 */
public class PageResponse<T> {

    /**
     * 当前页数据列表
     */
    private List<T> content;

    /**
     * 当前页码（从0开始）
     */
    private int pageNumber;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总元素数
     */
    private long totalElements;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 是否为第一页
     */
    private boolean first;

    /**
     * 是否为最后一页
     */
    private boolean last;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 是否有上一页
     */
    private boolean hasPrevious;

    // ==================== 构造方法 ====================

    public PageResponse() {
    }

    /**
     * 创建分页响应
     *
     * @param content       当前页数据
     * @param pageNumber    当前页码（从0开始）
     * @param pageSize      每页大小
     * @param totalElements 总元素数
     */
    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.first = pageNumber == 0;
        this.last = pageNumber >= totalPages - 1;
        this.hasNext = pageNumber < totalPages - 1;
        this.hasPrevious = pageNumber > 0;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        return new PageResponse<>(content, pageNumber, pageSize, totalElements);
    }

    /**
     * 创建空分页响应
     */
    public static <T> PageResponse<T> empty(int pageSize) {
        return new PageResponse<>();
    }

    // ==================== Getters and Setters ====================

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}