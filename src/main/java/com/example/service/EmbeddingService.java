package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文本向量化服务
 * 负责将文本转换为向量表示，支持缓存和异步处理
 */
@Service
public class EmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    /**
     * 生成文本的向量表示（带缓存）
     */
    @Cacheable(value = "textEmbeddings", key = "#text")
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("尝试为空文本生成向量");
            return new float[0];
        }
        
        try {
            logger.debug("生成文本向量: {}", text.substring(0, Math.min(text.length(), 50)));
            
            EmbeddingRequest request = EmbeddingRequest.builder()
                .input(List.of(text))
                .build();
                
            EmbeddingResponse response = embeddingModel.call(request);
            
            if (response.getResults().isEmpty()) {
                logger.error("向量生成失败，返回空结果");
                return new float[0];
            }
            
            float[] embedding = response.getResult().getOutput().toFloatArray();
            logger.debug("成功生成 {} 维向量", embedding.length);
            
            return embedding;
            
        } catch (Exception e) {
            logger.error("生成文本向量失败: {}", e.getMessage(), e);
            // 返回零向量作为降级方案
            return new float[1536]; // OpenAI embedding 默认维度
        }
    }
    
    /**
     * 异步生成向量（用于批量处理）
     */
    @Async
    public CompletableFuture<float[]> generateEmbeddingAsync(String text) {
        float[] embedding = generateEmbedding(text);
        return CompletableFuture.completedFuture(embedding);
    }
    
    /**
     * 批量生成向量
     */
    public List<float[]> generateBatchEmbeddings(List<String> texts) {
        logger.info("批量生成 {} 个文本的向量", texts.size());
        
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                .input(texts)
                .build();
                
            EmbeddingResponse response = embeddingModel.call(request);
            
            return response.getResults().stream()
                .map(result -> result.getOutput().toFloatArray())
                .toList();
                
        } catch (Exception e) {
            logger.error("批量生成向量失败: {}", e.getMessage(), e);
            // 降级为单个生成
            return texts.stream()
                .map(this::generateEmbedding)
                .toList();
        }
    }
    
    /**
     * 计算两个向量的余弦相似度
     */
    public double calculateCosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 组合多个向量（加权平均）
     */
    public float[] combineEmbeddings(float[] embedding1, double weight1, 
                                   float[] embedding2, double weight2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }
        
        float[] combined = new float[embedding1.length];
        double totalWeight = weight1 + weight2;
        
        for (int i = 0; i < embedding1.length; i++) {
            combined[i] = (float) ((embedding1[i] * weight1 + embedding2[i] * weight2) / totalWeight);
        }
        
        return combined;
    }
    
    /**
     * 检查向量服务是否可用
     */
    public boolean isServiceAvailable() {
        try {
            generateEmbedding("test");
            return true;
        } catch (Exception e) {
            logger.error("向量服务不可用: {}", e.getMessage());
            return false;
        }
    }
}