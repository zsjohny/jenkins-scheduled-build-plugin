#!/bin/bash

# Jenkins 预约构建插件 - 构建脚本
# 用途：自动构建和打包插件

set -e

echo "=========================================="
echo "🔨 Jenkins 预约构建插件 - 构建脚本"
echo "=========================================="
echo ""

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven，请先安装Maven"
    echo "下载地址: https://maven.apache.org/download.cgi"
    exit 1
fi

# 检查并设置 Java 11
echo "检查 Java 环境..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    export JAVA_HOME=$(/usr/libexec/java_home -v 11 2>/dev/null)
    if [ $? -ne 0 ]; then
        echo "❌ 错误: 未找到 Java 11"
        echo "请安装: brew install openjdk@11"
        exit 1
    fi
    echo "✓ 使用 Java 11"
else
    # Linux
    if [ -d "/usr/lib/jvm/java-11-openjdk-amd64" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
        echo "✓ 使用 Java 11"
    elif [ -d "/usr/lib/jvm/java-11-openjdk" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
        echo "✓ 使用 Java 11"
    else
        echo "⚠️  警告: 未找到 Java 11，使用系统默认 Java"
    fi
fi

echo ""
echo "Java 版本:"
java -version 2>&1 | head -1
echo ""
echo "Maven 版本:"
mvn -version | head -1
echo ""

# 构建插件（跳过测试以避免路径空格问题）
echo "步骤: 构建插件..."
echo "（跳过测试以加快构建速度并避免路径问题）"
echo ""
mvn clean package -DskipTests
echo ""
echo "✅ 构建完成"
echo ""

# 检查生成的文件
HPI_FILE="target/scheduled-build.hpi"
if [ -f "$HPI_FILE" ]; then
    FILE_SIZE=$(du -h "$HPI_FILE" | cut -f1)
    echo "=========================================="
    echo "✅ 构建成功！"
    echo "=========================================="
    echo ""
    echo "📦 插件文件: $HPI_FILE"
    echo "📊 文件大小: $FILE_SIZE"
    echo ""
    echo "=========================================="
    echo "📝 安装方法"
    echo "=========================================="
    echo "1. 登录 Jenkins"
    echo "2. 系统管理 → 插件管理 → 高级"
    echo "3. 上传插件区域选择上述文件"
    echo "4. 点击'上传'并重启 Jenkins"
    echo "5. 进入任意任务，左侧自动显示'预约构建'链接"
    echo ""
    echo "或直接复制到 Jenkins 插件目录："
    echo "  cp $HPI_FILE \$JENKINS_HOME/plugins/"
    echo "  然后重启 Jenkins"
    echo ""
    echo "=========================================="
    echo "🚀 快速测试"
    echo "=========================================="
    echo "./start-jenkins.sh    # 启动演示环境"
    echo ""
else
    echo "=========================================="
    echo "❌ 构建失败！"
    echo "=========================================="
    echo "未找到生成的 .hpi 文件"
    echo ""
    echo "💡 请检查上方构建日志中的错误信息"
    exit 1
fi



