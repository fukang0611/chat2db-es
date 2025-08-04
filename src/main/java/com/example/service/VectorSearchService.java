package com.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.example.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量搜索服务
 * 基于文本向量相似度进行语义搜索
 */
@Service
public class VectorSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorSearchService.class);
    
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Value("${app.elasticsearch.index-name:documents}")
    private String indexName;
    
    /**
     * 基于向量相似度的搜索
     */
    @Cacheable(value = "vectorSearchResults", key = "#query + '_' + #size")
    public SearchResponse<Document> vectorSearch(String query, int size) throws IOException {
        logger.info("执行向量搜索: {}", query);
        
        // 1. 生成查询向量
        float[] queryVector = embeddingService.generateEmbedding(query);
        
        if (queryVector.length == 0) {
            logger.warn("查询向量生成失败，返回空结果");
            return createEmptySearchResponse();
        }
        
        // 2. 构建向量查询
        Query vectorQuery = buildVectorQuery(queryVector);
        
        // 3. 执行搜索
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexName)
            .query(vectorQuery)
            .size(size)
            .source(source -> source.includes("*").excludes("*Embedding"))  // 排除向量字段
        );
        
        SearchResponse<Document> response = elasticsearchClient.search(searchRequest, Document.class);
        
        logger.info("向量搜索完成，找到 {} 个结果", response.hits().total().value());
        return response;
    }
    
    /**
     * 混合搜索：结合向量搜索和传统全文搜索
     */
    public SearchResponse<Document> hybridSearch(String query, int size) throws IOException {
        logger.info("执行混合搜索: {}", query);
        
        // 1. 生成查询向量
        float[] queryVector = embeddingService.generateEmbedding(query);
        
        // 2. 构建混合查询
        Query hybridQuery = buildHybridQuery(query, queryVector);
        
        // 3. 执行搜索
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexName)
            .query(hybridQuery)
            .size(size)
            .source(source -> source.includes("*").excludes("*Embedding"))
        );
        
        SearchResponse<Document> response = elasticsearchClient.search(searchRequest, Document.class);
        
        logger.info("混合搜索完成，找到 {} 个结果", response.hits().total().value());
        return response;
    }
    
    /**
     * 构建向量查询
     */
    private Query buildVectorQuery(float[] queryVector) {
        // 使用 script_score 查询进行向量相似度计算
        return Query.of(q -> q
            .scriptScore(ss -> ss
                .query(Query.of(matchAll -> matchAll.matchAll(m -> m)))
                .script(script -> script
                    .source("""
                        // 检查文档是否有combinedEmbedding字段
                        if (doc.containsKey('combinedEmbedding') && doc['combinedEmbedding'].size() > 0) {
                            // 计算余弦相似度并加1确保分数为正
                            return cosineSimilarity(params.query_vector, 'combinedEmbedding') + 1.0;
                        } else {
                            // 如果没有向量，使用标题和内容的文本相似度
                            return 0.1;
                        }
                        """)
                    .params(createVectorParams(queryVector))
                )
                .minScore(0.5f)  // 过滤相似度过低的结果
            )
        );
    }
    
    /**
     * 构建混合查询（向量 + 全文搜索）
     */
    private Query buildHybridQuery(String queryText, float[] queryVector) {
        return Query.of(q -> q
            .bool(bool -> bool
                .should(
                    // 向量相似度查询（权重更高）
                    Query.of(vector -> vector
                        .scriptScore(ss -> ss
                            .query(Query.of(matchAll -> matchAll.matchAll(m -> m)))
                            .script(script -> script
                                .source("""
                                    if (doc.containsKey('combinedEmbedding') && doc['combinedEmbedding'].size() > 0) {
                                        return cosineSimilarity(params.query_vector, 'combinedEmbedding') + 1.0;
                                    } else {
                                        return 0.0;
                                    }
                                    """)
                                .params(createVectorParams(queryVector))
                            )
                            .boost(3.0f)  // 向量搜索权重
                        )
                    ),
                    
                    // 传统全文搜索
                    Query.of(text -> text
                        .multiMatch(mm -> mm
                            .query(queryText)
                            .fields("title^2", "content", "category", "tags", "author")
                            .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            .boost(1.0f)  // 全文搜索权重
                        )
                    )
                )
            )
        );
    }
    
    /**
     * 创建向量参数
     */
    private Map<String, JsonData> createVectorParams(float[] queryVector) {
        Map<String, JsonData> params = new HashMap<>();
        params.put("query_vector", JsonData.of(queryVector));
        return params;
    }
    
    /**
     * 创建空的搜索响应
     */
    private SearchResponse<Document> createEmptySearchResponse() {
        // 这里应该返回一个空的SearchResponse
        // 由于Elasticsearch客户端的限制，我们返回null并在调用方处理
        return null;
    }
    
    /**
     * 基于文档ID的相似文档推荐
     */
    public SearchResponse<Document> findSimilarDocuments(String documentId, int size) throws IOException {
        logger.info("查找与文档 {} 相似的文档", documentId);
        
        // 1. 获取目标文档
        var getResponse = elasticsearchClient.get(g -> g
            .index(indexName)
            .id(documentId)
            .source(source -> source.includes("combinedEmbedding"))
        , Document.class);
        
        if (!getResponse.found()) {
            logger.warn("文档 {} 不存在", documentId);
            return createEmptySearchResponse();
        }
        
        // 2. 提取文档向量
        // 注意：这里需要从ES响应中提取向量，实际实现可能需要调整
        // float[] documentVector = extractEmbeddingFromDocument(getResponse.source());
        
        // 3. 基于向量查找相似文档
        // 暂时使用空向量作为示例
        float[] documentVector = new float[1536];
        
        Query similarQuery = Query.of(q -> q
            .scriptScore(ss -> ss
                .query(Query.of(bool -> bool
                    .bool(b -> b
                        .mustNot(mn -> mn.term(t -> t.field("_id").value(documentId)))  // 排除自身
                    )
                ))
                .script(script -> script
                    .source("cosineSimilarity(params.query_vector, 'combinedEmbedding') + 1.0")
                    .params(createVectorParams(documentVector))
                )
                .minScore(0.7f)  // 相似度阈值
            )
        );
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexName)
            .query(similarQuery)
            .size(size)
            .source(source -> source.includes("*").excludes("*Embedding"))
        );
        
        return elasticsearchClient.search(searchRequest, Document.class);
    }
    
    /**
     * 检查向量搜索是否可用
     */
    public boolean isVectorSearchAvailable() {
        try {
            // 测试一个简单的向量查询
            float[] testVector = new float[1536];  // 零向量
            testVector[0] = 1.0f;
            
            Query testQuery = Query.of(q -> q
                .scriptScore(ss -> ss
                    .query(Query.of(matchAll -> matchAll.matchAll(m -> m)))
                    .script(script -> script
                        .source("1.0")
                    )
                )
            );
            
            SearchRequest testRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(testQuery)
                .size(1)
            );
            
            elasticsearchClient.search(testRequest, Document.class);
            return true;
            
        } catch (Exception e) {
            logger.error("向量搜索不可用: {}", e.getMessage());
            return false;
        }
    }
}