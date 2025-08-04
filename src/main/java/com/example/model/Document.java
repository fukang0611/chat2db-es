package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch文档实体
 */
public class Document {
    
    private String id;
    
    private String title;
    
    private String content;
    
    private String category;
    
    private List<String> tags;
    
    @JsonProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    private String author;
    
    // 构造函数
    public Document() {}
    
    public Document(String title, String content, String category, List<String> tags, String author) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.author = author;
        this.createTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", tags=" + tags +
                ", createTime=" + createTime +
                ", author='" + author + '\'' +
                '}';
    }
}