package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * 查询请求DTO
 */
public class QueryRequest {
    
    @NotBlank(message = "查询内容不能为空")
    private String query;
    
    @Min(value = 0, message = "页码不能小于0")
    private int page = 0;
    
    @Min(value = 1, message = "页面大小不能小于1")
    private int size = 10;
    
    // 构造函数
    public QueryRequest() {}
    
    public QueryRequest(String query) {
        this.query = query;
    }
    
    public QueryRequest(String query, int page, int size) {
        this.query = query;
        this.page = page;
        this.size = size;
    }
    
    // Getter和Setter方法
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    @Override
    public String toString() {
        return "QueryRequest{" +
                "query='" + query + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}