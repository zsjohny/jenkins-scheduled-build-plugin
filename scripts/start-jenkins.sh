#!/bin/bash

echo "=========================================="
echo "🚀 启动 Jenkins 预约构建插件演示环境"
echo "=========================================="
echo ""

# 检查操作系统
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="macOS"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS_TYPE="Linux"
else
    OS_TYPE="Unknown"
fi
echo "操作系统: $OS_TYPE"
echo ""

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ 错误: Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查插件是否已构建
if [ ! -f "target/scheduled-build.hpi" ]; then
    echo "⚠️  插件文件不存在，开始构建..."
    echo ""
    
    # 设置 Java 11 环境（macOS）
    if [[ "$OSTYPE" == "darwin"* ]]; then
        export JAVA_HOME=$(/usr/libexec/java_home -v 11 2>/dev/null)
        if [ $? -ne 0 ]; then
            echo "❌ 错误: 未找到 Java 11，请先安装 Java 11"
            echo "   可以使用: brew install openjdk@11"
            exit 1
        fi
        echo "✓ 使用 Java 11: $JAVA_HOME"
    else
        # Linux: 尝试找到 Java 11
        if [ -d "/usr/lib/jvm/java-11-openjdk-amd64" ]; then
            export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
        elif [ -d "/usr/lib/jvm/java-11-openjdk" ]; then
            export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
        else
            echo "⚠️  警告: 未找到 Java 11，使用系统默认 Java"
        fi
    fi
    
    echo "正在构建插件..."
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ 插件构建失败"
        echo ""
        echo "💡 提示: 请确保使用 Java 11 构建"
        echo "   macOS: export JAVA_HOME=\$(/usr/libexec/java_home -v 11)"
        echo "   Linux: export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
        exit 1
    fi
    echo "✅ 插件构建成功"
fi

# 停止并删除旧容器
echo "清理旧环境..."
docker-compose down -v 2>/dev/null

# 启动Jenkins
echo ""
echo "启动 Jenkins 容器..."
docker-compose up -d

# 等待Jenkins启动
echo ""
echo "等待 Jenkins 启动..."
echo "这可能需要 30-60 秒..."
sleep 10

# 检查容器状态
if docker ps | grep -q jenkins-scheduled-build-demo; then
    echo ""
    echo "=========================================="
    echo "✅ Jenkins 启动成功！"
    echo "=========================================="
    echo ""
    echo "访问地址: http://localhost:8080"
    echo ""
    echo "登录信息:"
    echo "  用户名: admin"
    echo "  密码: admin123"
    echo ""
    echo "演示任务: scheduled-build-demo"
    echo ""
    echo "=========================================="
    echo "📝 使用步骤:"
    echo "=========================================="
    echo "1. 打开浏览器访问 http://localhost:8080"
    echo "2. 使用上述账号登录"
    echo "3. 点击 '系统管理' -> '插件管理' -> '高级'"
    echo "4. 上传插件文件: target/scheduled-build.hpi"
    echo "5. 重启 Jenkins"
    echo "6. 进入任意任务（如 'scheduled-build-demo'）"
    echo "7. ✨ 左侧菜单自动显示 '预约构建' 链接"
    echo "8. 点击 '预约构建' 添加预约并测试！"
    echo ""
    echo "💡 提示: 新版本插件已自动启用，无需手动配置"
    echo ""
    echo "=========================================="
    echo "📊 查看日志:"
    echo "=========================================="
    echo "docker logs -f jenkins-scheduled-build-demo"
    echo ""
    echo "=========================================="
    echo "⏹  停止环境:"
    echo "=========================================="
    echo "docker-compose down"
    echo ""
else
    echo ""
    echo "❌ Jenkins 启动失败，查看日志:"
    echo "docker logs jenkins-scheduled-build-demo"
    exit 1
fi

