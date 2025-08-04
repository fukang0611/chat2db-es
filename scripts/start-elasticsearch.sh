#!/bin/bash

# Elasticsearch 启动脚本

echo "🐳 启动 Elasticsearch Docker 容器..."

# 检查是否已有容器在运行
if docker ps -q -f name=elasticsearch | grep -q .; then
    echo "⚠️ Elasticsearch 容器已在运行"
    echo "容器状态:"
    docker ps -f name=elasticsearch
    exit 0
fi

# 检查是否有已停止的容器
if docker ps -aq -f name=elasticsearch | grep -q .; then
    echo "🔄 发现已停止的 Elasticsearch 容器，正在重启..."
    docker start elasticsearch
else
    echo "🆕 创建新的 Elasticsearch 容器..."
    docker run -d \
        --name elasticsearch \
        -p 9200:9200 \
        -p 9300:9300 \
        -e "discovery.type=single-node" \
        -e "xpack.security.enabled=false" \
        -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
        elasticsearch:8.11.1
fi

echo ""
echo "⏳ 等待 Elasticsearch 启动..."

# 等待服务启动
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:9200/_cluster/health &>/dev/null; then
        echo "✅ Elasticsearch 启动成功！"
        echo ""
        echo "📊 集群状态:"
        curl -s http://localhost:9200/_cluster/health | jq .
        echo ""
        echo "🌐 访问地址: http://localhost:9200"
        echo "🏥 健康检查: http://localhost:9200/_cluster/health"
        exit 0
    fi
    
    echo "等待中... (${attempt}/${max_attempts})"
    sleep 2
    ((attempt++))
done

echo "❌ Elasticsearch 启动超时"
echo "请检查 Docker 容器状态:"
docker logs elasticsearch --tail 20
exit 1