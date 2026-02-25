#!/bin/bash

# aiperm 后端启动脚本
# 使用方法: ./start-backend.sh

set -e

echo "🚀 启动 aiperm 后端服务..."

# 进入后端目录
cd "$(dirname "$0")/backend"

echo "🌐 启动 Spring Boot 服务..."
echo "   后端地址: http://localhost:8080"
echo "   Swagger:  http://localhost:8080/swagger-ui.html"
echo "   按 Ctrl+C 停止服务"
echo ""

./gradlew bootRun
