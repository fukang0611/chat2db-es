package com.example.service;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.dto.QueryRequest;
import com.example.dto.SearchResponse;
import com.example.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能搜索服务，组合AI查询转换和Elasticsearch搜索
 */
@Service
public class IntelligentSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(IntelligentSearchService.class);
    
    private final AiQueryService aiQueryService;
    private final ElasticsearchService elasticsearchService;
    
    public IntelligentSearchService(AiQueryService aiQueryService, 
                                   ElasticsearchService elasticsearchService) {
        this.aiQueryService = aiQueryService;
        this.elasticsearchService = elasticsearchService;
    }
    
    /**
     * 执行智能搜索
     * 
     * @param queryRequest 查询请求
     * @return 搜索响应
     */
    public com.example.dto.SearchResponse intelligentSearch(QueryRequest queryRequest) {
        logger.info("开始智能搜索: {}", queryRequest);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 使用AI将自然语言转换为DSL
            String generatedDsl = aiQueryService.convertToElasticsearchDsl(queryRequest.getQuery());
            
            // 2. 执行Elasticsearch查询
            int from = queryRequest.getPage() * queryRequest.getSize();
            SearchResponse<Document> esResponse = elasticsearchService.searchWithDsl(
                generatedDsl, from, queryRequest.getSize()
            );
            
            // 3. 提取文档结果
            List<Document> documents = elasticsearchService.extractDocuments(esResponse);
            
            // 4. 计算总耗时
            long took = System.currentTimeMillis() - startTime;
            
            // 5. 构建响应
            com.example.dto.SearchResponse response = new com.example.dto.SearchResponse(
                queryRequest.getQuery(),
                generatedDsl,
                documents,
                esResponse.hits().total().value(),
                queryRequest.getPage(),
                queryRequest.getSize(),
                took
            );
            
            logger.info("智能搜索完成: 找到{}个结果，耗时{}ms", 
                response.getTotalHits(), response.getTook());
            
            return response;
            
        } catch (Exception e) {
            logger.error("智能搜索失败: {}", e.getMessage(), e);
            
            // 返回错误响应
            long took = System.currentTimeMillis() - startTime;
            return new com.example.dto.SearchResponse(
                queryRequest.getQuery(),
                "查询失败: " + e.getMessage(),
                List.of(),
                0L,
                queryRequest.getPage(),
                queryRequest.getSize(),
                took
            );
        }
    }
    
    /**
     * 验证搜索系统状态
     */
    public boolean isSystemHealthy() {
        try {
            // 检查Elasticsearch连接
            long docCount = elasticsearchService.getDocumentCount();
            logger.info("系统健康检查: 索引中有{}个文档", docCount);
            return true;
        } catch (Exception e) {
            logger.error("系统健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 初始化搜索系统
     */
    public void initializeSystem() {
        try {
            logger.info("初始化智能搜索系统...");
            elasticsearchService.ensureIndexExists();
            logger.info("智能搜索系统初始化完成");
        } catch (Exception e) {
            logger.error("智能搜索系统初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索系统初始化失败", e);
        }
    }
}