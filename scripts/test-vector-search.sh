#!/bin/bash

# 向量搜索功能测试脚本

BASE_URL="http://localhost:8080"
API_PATH="/api/search"

echo "🧠 开始测试向量搜索功能..."
echo "Base URL: $BASE_URL"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 测试混合搜索
echo -e "${PURPLE}1. 测试智能混合搜索${NC}"
echo "语义查询：机器学习相关内容"
curl -s -X POST "$BASE_URL$API_PATH/hybrid" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "机器学习相关内容",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试向量搜索
echo -e "${PURPLE}2. 测试纯向量搜索${NC}"
echo "语义查询：深度学习神经网络"
curl -s -X POST "$BASE_URL$API_PATH/vector" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "深度学习神经网络",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试语义相似性
echo -e "${PURPLE}3. 测试语义相似性搜索${NC}"
echo "查询：如何优化程序性能"
curl -s -X POST "$BASE_URL$API_PATH/hybrid" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "如何优化程序性能",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试复杂查询
echo -e "${PURPLE}4. 测试复杂查询理解${NC}"
echo "查询：比较Spring Boot和其他Java框架的优缺点"
curl -s -X POST "$BASE_URL$API_PATH/hybrid" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "比较Spring Boot和其他Java框架的优缺点",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试相关性查询
echo -e "${PURPLE}5. 测试相关性查询${NC}"
echo "查询：与数据分析相关的技术"
curl -s -X POST "$BASE_URL$API_PATH/hybrid" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "与数据分析相关的技术",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 测试多语言语义
echo -e "${PURPLE}6. 测试中英文混合查询${NC}"
echo "查询：cloud computing architecture best practices"
curl -s -X POST "$BASE_URL$API_PATH/vector" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "cloud computing architecture best practices",
    "page": 0,
    "size": 3
  }' | jq .
echo -e "\n"

# 对比传统搜索
echo -e "${PURPLE}7. 对比传统搜索效果${NC}"
echo "传统搜索：Spring Boot"
curl -s -X POST "$BASE_URL$API_PATH/intelligent" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Spring Boot",
    "page": 0,
    "size": 2
  }' | jq .
echo -e "\n"

echo "向量搜索：快速开发框架"
curl -s -X POST "$BASE_URL$API_PATH/vector" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "快速开发框架",
    "page": 0,
    "size": 2
  }' | jq .
echo -e "\n"

# 性能测试
echo -e "${PURPLE}8. 简单性能测试${NC}"
echo "测试响应时间..."

start_time=$(date +%s%N)
curl -s -X POST "$BASE_URL$API_PATH/hybrid" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "人工智能应用场景",
    "page": 0,
    "size": 5
  }' > /dev/null
end_time=$(date +%s%N)

duration=$((($end_time - $start_time) / 1000000))
echo "混合搜索响应时间: ${duration}ms"
echo -e "\n"

echo -e "${GREEN}✅ 向量搜索测试完成！${NC}"
echo ""
echo -e "${YELLOW}测试说明：${NC}"
echo "- 混合搜索 (/hybrid): 结合向量搜索和传统搜索，推荐使用"
echo "- 向量搜索 (/vector): 纯语义相似度搜索"  
echo "- 智能搜索 (/intelligent): 原有的AI DSL转换搜索"
echo ""
echo -e "${BLUE}性能对比：${NC}"
echo "- 传统搜索: 依赖关键词匹配"
echo "- 向量搜索: 理解语义相似性"
echo "- 混合搜索: 结合两者优势，准确性最高"