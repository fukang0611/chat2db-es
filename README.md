# Elasticsearch AI Query - 智能问答系统

基于 Spring AI 和 Elasticsearch Java Client 9.x 的智能问答系统，可将用户的自然语言查询自动转换为 Elasticsearch DSL 查询。

## 🚀 项目特性

- **智能查询转换**: 使用 Spring AI (OpenAI GPT) 将自然语言转换为 Elasticsearch DSL
- **高性能搜索**: 基于 Elasticsearch Java Client 9.x 实现高效的全文搜索
- **RESTful API**: 提供完整的 REST API 接口
- **自动初始化**: 应用启动时自动创建索引和示例数据
- **健康检查**: 内置系统健康检查和监控
- **错误处理**: 完善的错误处理和降级机制

## 📋 技术栈

- **Spring Boot 3.2.0** - 应用框架
- **Spring AI 1.0.0-M3** - AI 集成框架
- **Elasticsearch Java Client 8.11.1** - Elasticsearch 客户端
- **OpenAI GPT-3.5-turbo** - 自然语言处理模型
- **Maven** - 依赖管理
- **Java 17** - 编程语言

## 🛠️ 环境要求

- Java 17+
- Maven 3.6+
- Elasticsearch 8.x
- OpenAI API Key

## ⚙️ 配置说明

### 1. 环境变量配置

```bash
# OpenAI API Key (必需)
export OPENAI_API_KEY=your-openai-api-key-here

# Elasticsearch 配置 (可选，默认值如下)
export ELASTICSEARCH_HOST=localhost
export ELASTICSEARCH_PORT=9200
export ELASTICSEARCH_SCHEME=http
export ELASTICSEARCH_USERNAME=
export ELASTICSEARCH_PASSWORD=
export ES_INDEX_NAME=documents
```

### 2. 应用配置

配置文件位于 `src/main/resources/application.yml`，主要配置项：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.1

elasticsearch:
  host: ${ELASTICSEARCH_HOST:localhost}
  port: ${ELASTICSEARCH_PORT:9200}
  scheme: ${ELASTICSEARCH_SCHEME:http}
```

## 🚀 快速开始

### 1. 启动 Elasticsearch

```bash
# 使用 Docker 启动 Elasticsearch
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  elasticsearch:8.11.1
```

### 2. 设置 OpenAI API Key

```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

### 3. 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## 📚 API 使用指南

### 1. 智能搜索

**POST** `/api/search/intelligent`

```bash
curl -X POST http://localhost:8080/api/search/intelligent \
  -H "Content-Type: application/json" \
  -d '{
    "query": "查找关于Spring Boot的文档",
    "page": 0,
    "size": 10
  }'
```

**GET** `/api/search/intelligent`

```bash
curl "http://localhost:8080/api/search/intelligent?query=人工智能&page=0&size=5"
```

### 2. 系统健康检查

```bash
curl http://localhost:8080/api/search/health
```

### 3. 添加文档

```bash
curl -X POST http://localhost:8080/api/search/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "新文档标题",
    "content": "文档内容...",
    "category": "技术文档",
    "tags": ["标签1", "标签2"],
    "author": "作者名"
  }'
```

### 4. 获取统计信息

```bash
curl http://localhost:8080/api/search/stats
```

## 🔍 查询示例

以下是一些自然语言查询示例：

1. **基础搜索**: "查找关于Spring Boot的文档"
2. **分类搜索**: "搜索技术教程类别的文章"
3. **作者搜索**: "找出张三写的所有文档"
4. **标签搜索**: "查找包含Java标签的文档"
5. **时间范围**: "查找最近创建的文档"
6. **复合查询**: "搜索关于人工智能且由王五撰写的文档"

系统会自动将这些自然语言查询转换为相应的 Elasticsearch DSL 查询。

## 📊 响应格式

智能搜索 API 的响应格式：

```json
{
  "originalQuery": "查找关于Spring Boot的文档",
  "generatedDsl": "{\"query\":{\"match\":{\"title\":\"Spring Boot\"}}}",
  "documents": [
    {
      "id": "doc1",
      "title": "Spring Boot 入门指南",
      "content": "文档内容...",
      "category": "技术教程",
      "tags": ["Spring", "Java"],
      "author": "张三",
      "createTime": "2024-01-01 10:00:00"
    }
  ],
  "totalHits": 1,
  "page": 0,
  "size": 10,
  "took": 245
}
```

## 🛡️ 错误处理

系统包含完善的错误处理机制：

1. **AI 转换失败**: 自动降级为基础的 multi_match 查询
2. **Elasticsearch 连接失败**: 返回友好的错误消息
3. **参数验证**: 自动验证请求参数的合法性
4. **超时处理**: 配置了合理的超时时间

## 🔧 开发和扩展

### 项目结构

```
src/main/java/com/example/
├── ElasticsearchAiQueryApplication.java  # 主启动类
├── config/
│   ├── ElasticsearchConfig.java          # ES 配置
│   └── ApplicationStartupListener.java   # 启动监听器
├── controller/
│   └── SearchController.java             # REST 控制器
├── dto/
│   ├── QueryRequest.java                 # 查询请求 DTO
│   └── SearchResponse.java               # 搜索响应 DTO
├── model/
│   └── Document.java                     # 文档实体
└── service/
    ├── AiQueryService.java               # AI 查询服务
    ├── ElasticsearchService.java         # ES 搜索服务
    ├── IntelligentSearchService.java     # 智能搜索服务
    └── SampleDataService.java            # 示例数据服务
```

### 自定义 AI 提示

可以在 `application.yml` 中修改 AI 系统提示词：

```yaml
app:
  ai:
    prompt:
      system: |
        你是一个Elasticsearch DSL查询专家...
        # 在这里自定义提示词
```

### 扩展文档字段

1. 修改 `Document.java` 实体类
2. 更新 `ElasticsearchService.java` 中的索引映射
3. 调整 AI 提示词以包含新字段

## 🐛 故障排除

### 常见问题

1. **OpenAI API 调用失败**
   - 检查 API Key 是否正确设置
   - 确认网络连接和代理设置

2. **Elasticsearch 连接失败**
   - 确认 Elasticsearch 服务正在运行
   - 检查连接配置和认证信息

3. **索引创建失败**
   - 检查 Elasticsearch 权限设置
   - 确认索引名称符合 ES 命名规范

### 日志配置

应用使用 SLF4J + Logback 记录日志，可在 `application.yml` 中调整日志级别：

```yaml
logging:
  level:
    com.example: DEBUG
    co.elastic.clients: DEBUG
```

## 📝 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 📞 支持

如有问题，请提交 GitHub Issue 或联系项目维护者。
