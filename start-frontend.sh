#!/bin/bash

# aiperm 前端启动脚本
# 使用方法: ./start-frontend.sh

set -e

echo "🚀 启动 aiperm 前端服务..."

# 进入前端目录
cd "$(dirname "$0")/frontend"

# 检查 node_modules 是否存在
if [ ! -d "node_modules" ]; then
    echo "📦 首次运行，安装依赖..."
    pnpm install
fi

# 启动开发服务器
echo "🌐 启动开发服务器..."
echo "   前端地址: http://localhost:5173"
echo "   按 Ctrl+C 停止服务"
echo ""

pnpm run dev
