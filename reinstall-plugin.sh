#!/bin/bash

set -e

echo "=========================================="
echo "🔄 完全重装插件（清理缓存）"
echo "=========================================="
echo ""

if [ ! -f "target/scheduled-build.hpi" ]; then
    echo "❌ 插件文件不存在，请先运行 ./build.sh"
    exit 1
fi

CONTAINER_NAME="jenkins-scheduled-build-demo"

echo "步骤 1/6: 停止并删除容器..."
docker-compose down -v
echo "✓ 容器和卷已删除"
echo ""

echo "步骤 2/6: 启动新容器..."
docker-compose up -d
echo "✓ 容器已启动"
echo ""

echo "步骤 3/6: 等待 Jenkins 初始化（15秒）..."
sleep 15
echo "✓ 初始化完成"
echo ""

echo "步骤 4/6: 安装插件..."
docker cp target/scheduled-build.hpi $CONTAINER_NAME:/var/jenkins_home/plugins/
echo "✓ 插件已复制"
echo ""

echo "步骤 5/6: 重启 Jenkins..."
docker restart $CONTAINER_NAME
echo "✓ 正在重启"
echo ""

echo "步骤 6/6: 等待完全启动（30秒）..."
sleep 30
echo "✓ 完成"
echo ""

echo "=========================================="
echo "✅ 完全重装成功！"
echo "=========================================="
echo ""
echo "🌐 访问: http://localhost:8080"
echo "👤 用户名: admin"
echo "🔑 密码: admin"
echo ""
echo "📝 测试步骤:"
echo "1. 进入任务 'scheduled-build-demo'"
echo "2. 左侧应该显示 '预约构建' 链接"
echo ""
echo "🔍 查看日志:"
echo "   docker logs -f $CONTAINER_NAME"
echo ""
