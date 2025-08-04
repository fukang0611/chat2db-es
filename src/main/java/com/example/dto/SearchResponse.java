package com.example.dto;

import com.example.model.Document;

import java.util.List;

/**
 * 搜索响应DTO
 */
public class SearchResponse {
    
    private String originalQuery;
    private String generatedDsl;
    private List<Document> documents;
    private long totalHits;
    private int page;
    private int size;
    private long took; // 查询耗时（毫秒）
    
    // 构造函数
    public SearchResponse() {}
    
    public SearchResponse(String originalQuery, String generatedDsl, List<Document> documents, 
                         long totalHits, int page, int size, long took) {
        this.originalQuery = originalQuery;
        this.generatedDsl = generatedDsl;
        this.documents = documents;
        this.totalHits = totalHits;
        this.page = page;
        this.size = size;
        this.took = took;
    }
    
    // Getter和Setter方法
    public String getOriginalQuery() {
        return originalQuery;
    }
    
    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }
    
    public String getGeneratedDsl() {
        return generatedDsl;
    }
    
    public void setGeneratedDsl(String generatedDsl) {
        this.generatedDsl = generatedDsl;
    }
    
    public List<Document> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    
    public long getTotalHits() {
        return totalHits;
    }
    
    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
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
    
    public long getTook() {
        return took;
    }
    
    public void setTook(long took) {
        this.took = took;
    }
    
    @Override
    public String toString() {
        return "SearchResponse{" +
                "originalQuery='" + originalQuery + '\'' +
                ", generatedDsl='" + generatedDsl + '\'' +
                ", documentsCount=" + (documents != null ? documents.size() : 0) +
                ", totalHits=" + totalHits +
                ", page=" + page +
                ", size=" + size +
                ", took=" + took +
                '}';
    }
}