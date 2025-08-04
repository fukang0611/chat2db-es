package com.example.controller;

import com.example.dto.QueryRequest;
import com.example.dto.SearchResponse;
import com.example.model.Document;
import com.example.service.ElasticsearchService;
import com.example.service.IntelligentSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 智能搜索REST API控制器
 */
@RestController
@RequestMapping("/api/search")
@Validated
@CrossOrigin(origins = "*")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    private final IntelligentSearchService intelligentSearchService;
    private final ElasticsearchService elasticsearchService;
    
    public SearchController(IntelligentSearchService intelligentSearchService,
                           ElasticsearchService elasticsearchService) {
        this.intelligentSearchService = intelligentSearchService;
        this.elasticsearchService = elasticsearchService;
    }
    
    /**
     * 智能搜索接口
     */
    @PostMapping("/intelligent")
    public ResponseEntity<SearchResponse> intelligentSearch(@Valid @RequestBody QueryRequest queryRequest) {
        logger.info("收到智能搜索请求: {}", queryRequest);
        
        try {
            SearchResponse response = intelligentSearchService.intelligentSearch(queryRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("智能搜索处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchResponse(
                    queryRequest.getQuery(),
                    "系统错误: " + e.getMessage(),
                    List.of(),
                    0L,
                    queryRequest.getPage(),
                    queryRequest.getSize(),
                    0L
                ));
        }
    }
    
    /**
     * 简化的GET搜索接口
     */
    @GetMapping("/intelligent")
    public ResponseEntity<SearchResponse> intelligentSearchGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        QueryRequest queryRequest = new QueryRequest(query, page, size);
        return intelligentSearch(queryRequest);
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("执行健康检查");
        
        boolean isHealthy = intelligentSearchService.isSystemHealthy();
        
        Map<String, Object> health = Map.of(
            "status", isHealthy ? "UP" : "DOWN",
            "elasticsearch", isHealthy ? "连接正常" : "连接异常",
            "timestamp", System.currentTimeMillis()
        );
        
        HttpStatus status = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }
    
    /**
     * 添加文档接口（用于测试）
     */
    @PostMapping("/documents")
    public ResponseEntity<Map<String, String>> addDocument(@RequestBody Document document) {
        logger.info("添加文档: {}", document.getTitle());
        
        try {
            elasticsearchService.indexDocument(document);
            return ResponseEntity.ok(Map.of(
                "message", "文档添加成功",
                "title", document.getTitle()
            ));
        } catch (Exception e) {
            logger.error("添加文档失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "添加文档失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量添加文档接口
     */
    @PostMapping("/documents/batch")
    public ResponseEntity<Map<String, Object>> addDocuments(@RequestBody List<Document> documents) {
        logger.info("批量添加 {} 个文档", documents.size());
        
        try {
            elasticsearchService.indexDocuments(documents);
            return ResponseEntity.ok(Map.of(
                "message", "批量添加成功",
                "count", documents.size()
            ));
        } catch (Exception e) {
            logger.error("批量添加文档失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "批量添加失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有文档接口（用于测试）
     */
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            int from = page * size;
            var esResponse = elasticsearchService.searchAll(from, size);
            List<Document> documents = elasticsearchService.extractDocuments(esResponse);
            
            return ResponseEntity.ok(Map.of(
                "documents", documents,
                "total", esResponse.hits().total().value(),
                "page", page,
                "size", size
            ));
        } catch (Exception e) {
            logger.error("获取文档失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "获取文档失败: " + e.getMessage()));
        }
    }
    
    /**
     * 系统统计信息接口
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            long documentCount = elasticsearchService.getDocumentCount();
            
            return ResponseEntity.ok(Map.of(
                "documentCount", documentCount,
                "indexName", "documents",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "获取统计信息失败: " + e.getMessage()));
        }
    }
}