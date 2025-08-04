package com.example.service;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.dto.QueryRequest;
import com.example.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合搜索服务
 * 整合向量搜索、传统全文搜索和AI增强查询
 */
@Service
public class HybridSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(HybridSearchService.class);
    
    @Autowired
    private VectorSearchService vectorSearchService;
    
    @Autowired
    private ElasticsearchService elasticsearchService;
    
    @Autowired
    private AiQueryService aiQueryService;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    /**
     * 智能混合搜索
     * 根据查询复杂度选择最优搜索策略
     */
    @Cacheable(value = "hybridSearchResults", key = "#queryRequest.query + '_' + #queryRequest.page + '_' + #queryRequest.size")
    public com.example.dto.SearchResponse smartSearch(QueryRequest queryRequest) {
        logger.info("开始智能混合搜索: {}", queryRequest.getQuery());
        
        long startTime = System.currentTimeMillis();
        String query = queryRequest.getQuery();
        
        try {
            // 1. 分析查询复杂度和意图
            SearchStrategy strategy = determineSearchStrategy(query);
            logger.info("选择搜索策略: {}", strategy);
            
            // 2. 执行相应的搜索策略
            List<Document> results = executeSearchStrategy(strategy, queryRequest);
            
            // 3. 计算耗时
            long took = System.currentTimeMillis() - startTime;
            
            // 4. 构建响应
            return new com.example.dto.SearchResponse(
                query,
                "Hybrid Search - " + strategy.name(),
                results,
                results.size(),
                queryRequest.getPage(),
                queryRequest.getSize(),
                took
            );
            
        } catch (Exception e) {
            logger.error("混合搜索失败: {}", e.getMessage(), e);
            
            // 降级到基础搜索
            return fallbackSearch(queryRequest);
        }
    }
    
    /**
     * 确定搜索策略
     */
    private SearchStrategy determineSearchStrategy(String query) {
        // 简单的策略选择逻辑，可以用AI增强
        if (isComplexQuery(query)) {
            return SearchStrategy.AI_ENHANCED;
        } else if (isSemanticQuery(query)) {
            return SearchStrategy.VECTOR_FIRST;
        } else {
            return SearchStrategy.HYBRID_BALANCED;
        }
    }
    
    /**
     * 执行选定的搜索策略
     */
    private List<Document> executeSearchStrategy(SearchStrategy strategy, QueryRequest queryRequest) throws Exception {
        int from = queryRequest.getPage() * queryRequest.getSize();
        
        return switch (strategy) {
            case VECTOR_FIRST -> executeVectorFirstSearch(queryRequest.getQuery(), from, queryRequest.getSize());
            case TEXT_FIRST -> executeTextFirstSearch(queryRequest.getQuery(), from, queryRequest.getSize());
            case HYBRID_BALANCED -> executeHybridBalancedSearch(queryRequest.getQuery(), from, queryRequest.getSize());
            case AI_ENHANCED -> executeAIEnhancedSearch(queryRequest.getQuery(), from, queryRequest.getSize());
        };
    }
    
    /**
     * 向量优先搜索
     */
    private List<Document> executeVectorFirstSearch(String query, int from, int size) throws Exception {
        logger.debug("执行向量优先搜索");
        
        // 1. 向量搜索
        SearchResponse<Document> vectorResponse = vectorSearchService.vectorSearch(query, size * 2);
        List<Document> vectorResults = extractDocuments(vectorResponse);
        
        if (vectorResults.size() >= size) {
            return vectorResults.subList(0, size);
        }
        
        // 2. 如果向量搜索结果不足，补充传统搜索
        SearchResponse<Document> textResponse = elasticsearchService.searchAll(from, size - vectorResults.size());
        List<Document> textResults = elasticsearchService.extractDocuments(textResponse);
        
        // 3. 去重并合并
        return mergeAndDeduplicateResults(vectorResults, textResults, size);
    }
    
    /**
     * 传统搜索优先
     */
    private List<Document> executeTextFirstSearch(String query, int from, int size) throws Exception {
        logger.debug("执行传统搜索优先");
        
        // 构建简单的全文搜索查询
        String dsl = String.format("""
            {
              "query": {
                "multi_match": {
                  "query": "%s",
                  "fields": ["title^2", "content", "category", "tags", "author"]
                }
              }
            }
            """, query.replace("\"", "\\\""));
        
        SearchResponse<Document> response = elasticsearchService.searchWithDsl(dsl, from, size);
        return elasticsearchService.extractDocuments(response);
    }
    
    /**
     * 平衡混合搜索
     */
    private List<Document> executeHybridBalancedSearch(String query, int from, int size) throws Exception {
        logger.debug("执行平衡混合搜索");
        
        SearchResponse<Document> response = vectorSearchService.hybridSearch(query, size);
        return extractDocuments(response);
    }
    
    /**
     * AI增强搜索
     */
    private List<Document> executeAIEnhancedSearch(String query, int from, int size) throws Exception {
        logger.debug("执行AI增强搜索");
        
        // 1. 使用AI生成DSL
        String aiGeneratedDsl = aiQueryService.convertToElasticsearchDsl(query);
        
        // 2. 执行AI生成的查询
        SearchResponse<Document> aiResponse = elasticsearchService.searchWithDsl(aiGeneratedDsl, from, size);
        List<Document> aiResults = elasticsearchService.extractDocuments(aiResponse);
        
        if (aiResults.size() >= size) {
            return aiResults;
        }
        
        // 3. 如果AI查询结果不足，补充向量搜索
        SearchResponse<Document> vectorResponse = vectorSearchService.vectorSearch(query, size - aiResults.size());
        List<Document> vectorResults = extractDocuments(vectorResponse);
        
        return mergeAndDeduplicateResults(aiResults, vectorResults, size);
    }
    
    /**
     * 结果合并和去重
     */
    private List<Document> mergeAndDeduplicateResults(List<Document> primaryResults, 
                                                     List<Document> secondaryResults, 
                                                     int maxSize) {
        Set<String> seenIds = new HashSet<>();
        List<Document> merged = new ArrayList<>();
        
        // 添加主要结果
        for (Document doc : primaryResults) {
            if (merged.size() >= maxSize) break;
            String key = generateDocumentKey(doc);
            if (seenIds.add(key)) {
                merged.add(doc);
            }
        }
        
        // 添加次要结果
        for (Document doc : secondaryResults) {
            if (merged.size() >= maxSize) break;
            String key = generateDocumentKey(doc);
            if (seenIds.add(key)) {
                merged.add(doc);
            }
        }
        
        return merged;
    }
    
    /**
     * 生成文档唯一标识
     */
    private String generateDocumentKey(Document doc) {
        // 使用标题和作者组合作为去重键
        return doc.getTitle() + "_" + doc.getAuthor();
    }
    
    /**
     * 判断是否为复杂查询
     */
    private boolean isComplexQuery(String query) {
        // 简单的启发式规则
        return query.contains("AND") || query.contains("OR") || 
               query.contains("统计") || query.contains("聚合") ||
               query.contains("分析") || query.contains("对比") ||
               query.length() > 50;
    }
    
    /**
     * 判断是否为语义查询
     */
    private boolean isSemanticQuery(String query) {
        // 检查是否包含语义相关的词汇
        String[] semanticKeywords = {"相似", "类似", "相关", "相近", "如何", "什么", "为什么"};
        for (String keyword : semanticKeywords) {
            if (query.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 降级搜索（当所有策略都失败时）
     */
    private com.example.dto.SearchResponse fallbackSearch(QueryRequest queryRequest) {
        logger.warn("执行降级搜索");
        
        try {
            int from = queryRequest.getPage() * queryRequest.getSize();
            SearchResponse<Document> response = elasticsearchService.searchAll(from, queryRequest.getSize());
            List<Document> documents = elasticsearchService.extractDocuments(response);
            
            return new com.example.dto.SearchResponse(
                queryRequest.getQuery(),
                "Fallback Search",
                documents,
                response.hits().total().value(),
                queryRequest.getPage(),
                queryRequest.getSize(),
                0L
            );
            
        } catch (Exception e) {
            logger.error("降级搜索也失败了: {}", e.getMessage());
            
            return new com.example.dto.SearchResponse(
                queryRequest.getQuery(),
                "Search Failed",
                Collections.emptyList(),
                0L,
                queryRequest.getPage(),
                queryRequest.getSize(),
                0L
            );
        }
    }
    
    /**
     * 从搜索响应中提取文档
     */
    private List<Document> extractDocuments(SearchResponse<Document> response) {
        if (response == null || response.hits() == null) {
            return Collections.emptyList();
        }
        
        return response.hits().hits().stream()
            .map(hit -> hit.source())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 搜索策略枚举
     */
    public enum SearchStrategy {
        VECTOR_FIRST,      // 向量搜索优先
        TEXT_FIRST,        // 传统搜索优先
        HYBRID_BALANCED,   // 平衡混合搜索
        AI_ENHANCED        // AI增强搜索
    }
}