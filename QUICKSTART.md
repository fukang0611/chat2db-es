# 🚀 快速启动指南

## 前提条件

1. ✅ Java 17+ 已安装
2. ✅ Docker 已安装（用于运行 Elasticsearch）
3. ✅ 拥有 OpenAI API Key

## 🏃‍♂️ 3 分钟启动

### 步骤 1: 启动 Elasticsearch

```bash
# 运行Elasticsearch启动脚本
./scripts/start-elasticsearch.sh

# 或手动运行Docker命令
docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  elasticsearch:8.11.1
```

### 步骤 2: 设置 OpenAI API Key

```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

### 步骤 3: 启动应用

```bash
# 如果有Maven
mvn spring-boot:run

# 或者直接运行JAR包（如果已编译）
java -jar target/elasticsearch-ai-query-1.0.0.jar
```

### 步骤 4: 测试系统

```bash
# 运行测试脚本
./scripts/test-api.sh

# 或手动测试健康检查
curl http://localhost:8080/api/search/health
```

## 🧪 快速测试

### 智能搜索测试

```bash
# 搜索Spring Boot相关内容
curl -X POST http://localhost:8080/api/search/intelligent \
  -H "Content-Type: application/json" \
  -d '{"query": "查找关于Spring Boot的文档", "size": 3}'

# 搜索人工智能相关内容  
curl "http://localhost:8080/api/search/intelligent?query=人工智能&size=2"
```

### 查看系统状态

```bash
# 健康检查
curl http://localhost:8080/api/search/health

# 统计信息
curl http://localhost:8080/api/search/stats
```

## 🔍 查询示例

试试这些自然语言查询：

- `"查找关于Spring Boot的文档"`
- `"搜索张三写的所有文章"`
- `"找出技术教程类别的内容"`
- `"查找包含Java标签的文档"`
- `"搜索关于人工智能的文章"`

## ⚡ 常见问题

**Q: 应用启动失败？**
A: 检查 OpenAI API Key 和 Elasticsearch 连接

**Q: 搜索结果为空？**
A: 应用启动时会自动创建示例数据，等待几秒钟

**Q: AI 转换失败？**
A: 系统会自动降级为基础搜索，检查网络和 API Key

## 🌐 主要接口

- **智能搜索**: `POST /api/search/intelligent`
- **健康检查**: `GET /api/search/health`  
- **添加文档**: `POST /api/search/documents`
- **获取统计**: `GET /api/search/stats`

---

🎉 **恭喜！** 你的智能问答系统已经运行起来了！

详细文档请查看 [README.md](README.md)