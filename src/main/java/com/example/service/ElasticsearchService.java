package com.example.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.example.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch搜索服务
 */
@Service
public class ElasticsearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);
    
    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;
    
    @Value("${app.elasticsearch.index-name:documents}")
    private String indexName;
    
    public ElasticsearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 确保索引存在
     */
    public void ensureIndexExists() throws IOException {
        logger.info("检查索引是否存在: {}", indexName);
        
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
        boolean exists = elasticsearchClient.indices().exists(existsRequest).value();
        
        if (!exists) {
            logger.info("索引不存在，正在创建: {}", indexName);
            createIndex();
        } else {
            logger.info("索引已存在: {}", indexName);
        }
    }
    
    /**
     * 创建索引
     */
    private void createIndex() throws IOException {
        // 定义索引映射
        String mapping = """
            {
              "properties": {
                "title": {
                  "type": "text",
                  "analyzer": "standard",
                  "fields": {
                    "keyword": {
                      "type": "keyword"
                    }
                  }
                },
                "content": {
                  "type": "text",
                  "analyzer": "standard"
                },
                "category": {
                  "type": "keyword"
                },
                "tags": {
                  "type": "keyword"
                },
                "createTime": {
                  "type": "date",
                  "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
                },
                "author": {
                  "type": "keyword"
                }
              }
            }
            """;
        
        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
            .index(indexName)
            .mappings(m -> m.withJson(new StringReader(mapping)))
        );
        
        elasticsearchClient.indices().create(createIndexRequest);
        logger.info("索引创建成功: {}", indexName);
    }
    
    /**
     * 执行DSL查询
     */
    public SearchResponse<Document> searchWithDsl(String dslJson, int from, int size) throws IOException {
        logger.info("执行DSL查询: {}", dslJson);
        
        // 解析DSL查询
        Query query = Query.of(q -> q.withJson(new StringReader(dslJson)));
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexName)
            .query(query)
            .from(from)
            .size(size)
        );
        
        SearchResponse<Document> response = elasticsearchClient.search(searchRequest, Document.class);
        
        logger.info("查询完成，找到 {} 个结果，耗时: {}ms", 
            response.hits().total().value(), response.took());
        
        return response;
    }
    
    /**
     * 将搜索结果转换为文档列表
     */
    public List<Document> extractDocuments(SearchResponse<Document> searchResponse) {
        return searchResponse.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }
    
    /**
     * 索引单个文档
     */
    public void indexDocument(Document document) throws IOException {
        logger.info("索引文档: {}", document.getTitle());
        
        IndexRequest<Document> indexRequest = IndexRequest.of(i -> i
            .index(indexName)
            .document(document)
        );
        
        var response = elasticsearchClient.index(indexRequest);
        logger.info("文档索引成功，ID: {}", response.id());
    }
    
    /**
     * 批量索引文档
     */
    public void indexDocuments(List<Document> documents) throws IOException {
        logger.info("批量索引 {} 个文档", documents.size());
        
        for (Document document : documents) {
            indexDocument(document);
        }
        
        logger.info("批量索引完成");
    }
    
    /**
     * 执行原始查询（用于测试）
     */
    public SearchResponse<Document> searchAll(int from, int size) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexName)
            .query(q -> q.matchAll(m -> m))
            .from(from)
            .size(size)
        );
        
        return elasticsearchClient.search(searchRequest, Document.class);
    }
    
    /**
     * 获取索引统计信息
     */
    public long getDocumentCount() throws IOException {
        var response = searchAll(0, 0);
        return response.hits().total().value();
    }
}