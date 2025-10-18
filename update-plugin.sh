#!/bin/bash

echo "=========================================="
echo "🔄 更新 Jenkins 插件"
echo "=========================================="
echo ""

# 检查插件文件
if [ ! -f "target/scheduled-build.hpi" ]; then
    echo "❌ 插件文件不存在，请先运行 ./build.sh"
    exit 1
fi

CONTAINER_NAME="jenkins-scheduled-build-demo"

# 1. 检查容器状态
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "步骤 1/5: 停止 Jenkins 容器..."
    docker stop $CONTAINER_NAME
    echo "✓ 容器已停止"
    echo ""
    
    echo "步骤 2/5: 删除旧插件..."
    docker exec $CONTAINER_NAME rm -f /var/jenkins_home/plugins/scheduled-build.hpi 2>/dev/null || true
    docker exec $CONTAINER_NAME rm -rf /var/jenkins_home/plugins/scheduled-build/ 2>/dev/null || true
    echo "✓ 旧插件已清理"
    echo ""
else
    echo "步骤 1/5: 启动 Jenkins 容器..."
    docker-compose up -d
    echo "✓ 容器已启动"
    echo ""
    sleep 5
    echo "步骤 2/5: 跳过（首次启动）"
    echo ""
fi

# 3. 启动容器（如果已停止）
echo "步骤 3/5: 确保容器运行中..."
docker-compose up -d
echo "✓ 容器运行中"
echo ""

# 4. 等待容器就绪
echo "步骤 4/5: 等待容器就绪..."
sleep 5

# 5. 复制新插件到容器
echo "步骤 5/5: 安装新插件..."
docker cp target/scheduled-build.hpi $CONTAINER_NAME:/var/jenkins_home/plugins/scheduled-build.hpi
echo "✓ 插件已复制到容器"
echo ""

# 6. 重启 Jenkins（完全重启以加载插件）
echo "重启 Jenkins 以加载插件..."
docker restart $CONTAINER_NAME
echo "✓ Jenkins 正在重启..."
echo ""

echo "=========================================="
echo "✅ 更新完成！"
echo "=========================================="
echo ""
echo "⏳ 等待 30-60 秒让 Jenkins 完全启动，然后："
echo ""
echo "1️⃣  访问: http://localhost:8080"
echo "2️⃣  进入任意任务"
echo "3️⃣  点击左侧 '预约构建' 链接"
echo ""
echo "🔍 查看日志："
echo "   docker logs -f $CONTAINER_NAME"
echo ""
echo "✅ 验证管理器初始化："
echo "   进入 Jenkins Script Console 运行："
echo "   println ScheduledBuildManager.get()"
echo ""
