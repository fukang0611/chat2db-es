package com.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI查询服务，负责将自然语言转换为Elasticsearch DSL
 */
@Service
public class AiQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(AiQueryService.class);
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai.prompt.system}")
    private String systemPrompt;
    
    public AiQueryService(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 将自然语言查询转换为Elasticsearch DSL
     * 
     * @param naturalLanguageQuery 自然语言查询
     * @return Elasticsearch DSL JSON字符串
     */
    public String convertToElasticsearchDsl(String naturalLanguageQuery) {
        logger.info("开始转换自然语言查询: {}", naturalLanguageQuery);
        
        try {
            // 构建完整的提示
            String userPrompt = buildUserPrompt(naturalLanguageQuery);
            
            // 创建提示模板
            PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "\n\n用户查询: {query}");
            Prompt prompt = promptTemplate.create(Map.of("query", naturalLanguageQuery));
            
            // 调用AI模型
            String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            
            logger.debug("AI模型原始响应: {}", aiResponse);
            
            // 清理和验证AI响应
            String cleanedDsl = cleanAndValidateDsl(aiResponse);
            
            logger.info("成功转换为DSL: {}", cleanedDsl);
            return cleanedDsl;
            
        } catch (Exception e) {
            logger.error("转换自然语言查询失败: {}", e.getMessage(), e);
            // 返回一个默认的查询
            return createFallbackQuery(naturalLanguageQuery);
        }
    }
    
    /**
     * 构建用户提示
     */
    private String buildUserPrompt(String query) {
        return String.format("""
            请将以下自然语言查询转换为Elasticsearch DSL：
            
            "%s"
            
            请直接返回JSON格式的DSL，不要包含任何解释文字。
            """, query);
    }
    
    /**
     * 清理和验证DSL响应
     */
    private String cleanAndValidateDsl(String aiResponse) throws JsonProcessingException {
        // 移除可能的markdown代码块标记
        String cleaned = aiResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        cleaned = cleaned.trim();
        
        // 验证JSON格式
        try {
            JsonNode jsonNode = objectMapper.readTree(cleaned);
            // 确保包含query字段
            if (!jsonNode.has("query")) {
                throw new JsonProcessingException("DSL must contain 'query' field") {};
            }
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            logger.warn("AI响应不是有效的JSON，尝试修复: {}", cleaned);
            throw e;
        }
    }
    
    /**
     * 创建备用查询（当AI转换失败时使用）
     */
    private String createFallbackQuery(String query) {
        logger.warn("使用备用查询模式");
        
        try {
            // 创建一个简单的multi_match查询
            Map<String, Object> multiMatch = Map.of(
                "multi_match", Map.of(
                    "query", query,
                    "fields", new String[]{"title^2", "content", "category", "tags", "author"}
                )
            );
            
            Map<String, Object> dslQuery = Map.of("query", multiMatch);
            
            return objectMapper.writeValueAsString(dslQuery);
        } catch (JsonProcessingException e) {
            logger.error("创建备用查询失败", e);
            // 最简单的查询
            return "{\"query\":{\"match_all\":{}}}";
        }
    }
    
    /**
     * 验证DSL查询的合法性
     */
    public boolean validateDsl(String dsl) {
        try {
            JsonNode jsonNode = objectMapper.readTree(dsl);
            return jsonNode.has("query");
        } catch (Exception e) {
            logger.error("DSL验证失败: {}", e.getMessage());
            return false;
        }
    }
}