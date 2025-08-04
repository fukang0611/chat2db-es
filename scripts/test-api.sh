#!/bin/bash

# Elasticsearch AI Query 系统测试脚本

BASE_URL="http://localhost:8080"
API_PATH="/api/search"

echo "🚀 开始测试 Elasticsearch AI Query 系统..."
echo "Base URL: $BASE_URL"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试健康检查
echo -e "${BLUE}1. 测试系统健康检查${NC}"
echo "GET $BASE_URL$API_PATH/health"
curl -s -X GET "$BASE_URL$API_PATH/health" | jq .
echo -e "\n"

# 测试统计信息
echo -e "${BLUE}2. 获取系统统计信息${NC}"
echo "GET $BASE_URL$API_PATH/stats"
curl -s -X GET "$BASE_URL$API_PATH/stats" | jq .
echo -e "\n"

# 测试智能搜索 - Spring Boot
echo -e "${BLUE}3. 智能搜索测试 - 查找Spring Boot相关文档${NC}"
echo "POST $BASE_URL$API_PATH/intelligent"
curl -s -X POST "$BASE_URL$API_PATH/intelligent" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "查找关于Spring Boot的文档",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试智能搜索 - 人工智能
echo -e "${BLUE}4. 智能搜索测试 - 人工智能相关内容${NC}"
echo "GET $BASE_URL$API_PATH/intelligent?query=人工智能&size=2"
curl -s -G "$BASE_URL$API_PATH/intelligent" \
  --data-urlencode "query=人工智能" \
  --data-urlencode "size=2" | jq .
echo -e "\n"

# 测试智能搜索 - 作者搜索
echo -e "${BLUE}5. 智能搜索测试 - 按作者搜索${NC}"
echo "搜索张三的文档"
curl -s -X POST "$BASE_URL$API_PATH/intelligent" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "找出张三写的所有文档",
    "page": 0,
    "size": 5
  }' | jq .
echo -e "\n"

# 测试智能搜索 - 分类搜索
echo -e "${BLUE}6. 智能搜索测试 - 按分类搜索${NC}"
echo "搜索技术教程类别的文档"
curl -s -X POST "$BASE_URL$API_PATH/intelligent" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "搜索技术教程类别的文章",
    "page": 0,
    "size": 5
  }' | jq .
echo -e "\n"

# 测试智能搜索 - 标签搜索
echo -e "${BLUE}7. 智能搜索测试 - 按标签搜索${NC}"
echo "查找包含Java标签的文档"
curl -s -X POST "$BASE_URL$API_PATH/intelligent" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "查找包含Java标签的文档",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试添加新文档
echo -e "${BLUE}8. 测试添加新文档${NC}"
echo "POST $BASE_URL$API_PATH/documents"
curl -s -X POST "$BASE_URL$API_PATH/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "API测试文档",
    "content": "这是一个通过API测试脚本添加的文档，用于验证系统的文档添加功能。该文档包含了关于API测试、自动化测试、系统验证等相关内容。",
    "category": "测试文档",
    "tags": ["API测试", "自动化", "验证"],
    "author": "测试用户"
  }' | jq .
echo -e "\n"

# 等待索引刷新
echo -e "${YELLOW}等待 2 秒让 Elasticsearch 刷新索引...${NC}"
sleep 2

# 搜索刚添加的文档
echo -e "${BLUE}9. 搜索刚添加的文档${NC}"
echo "搜索API测试相关文档"
curl -s -X POST "$BASE_URL$API_PATH/intelligent" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "查找关于API测试的文档",
    "page": 0,
    "size": 5
  }' | jq .
echo -e "\n"

# 获取所有文档
echo -e "${BLUE}10. 获取所有文档（前5个）${NC}"
echo "GET $BASE_URL$API_PATH/documents?size=5"
curl -s -G "$BASE_URL$API_PATH/documents" \
  --data-urlencode "size=5" | jq .
echo -e "\n"

echo -e "${GREEN}✅ 测试完成！${NC}"
echo ""
echo -e "${YELLOW}提示：${NC}"
echo "- 确保已设置 OPENAI_API_KEY 环境变量"
echo "- 确保 Elasticsearch 在 localhost:9200 运行"
echo "- 如需修改测试参数，请编辑此脚本文件"