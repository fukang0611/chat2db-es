package com.example.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Elasticsearch配置类
 */
@Configuration
public class ElasticsearchConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);
    
    @Value("${elasticsearch.host:localhost}")
    private String host;
    
    @Value("${elasticsearch.port:9200}")
    private int port;
    
    @Value("${elasticsearch.scheme:http}")
    private String scheme;
    
    @Value("${elasticsearch.username:}")
    private String username;
    
    @Value("${elasticsearch.password:}")
    private String password;
    
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        logger.info("正在初始化Elasticsearch客户端: {}://{}:{}", scheme, host, port);
        
        HttpHost httpHost = new HttpHost(host, port, scheme);
        
        RestClientBuilder builder = RestClient.builder(httpHost);
        
        // 如果提供了用户名和密码，则配置认证
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            logger.info("配置Elasticsearch认证: {}", username);
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY, 
                new UsernamePasswordCredentials(username, password)
            );
            
            builder.setHttpClientConfigCallback(httpClientBuilder -> 
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }
        
        // 配置超时时间
        builder.setRequestConfigCallback(requestConfigBuilder -> 
            requestConfigBuilder
                .setConnectTimeout(5000)
                .setSocketTimeout(60000)
        );
        
        RestClient restClient = builder.build();
        
        // 创建传输层
        RestClientTransport transport = new RestClientTransport(
            restClient, 
            new JacksonJsonpMapper()
        );
        
        ElasticsearchClient client = new ElasticsearchClient(transport);
        
        logger.info("Elasticsearch客户端初始化完成");
        return client;
    }
}