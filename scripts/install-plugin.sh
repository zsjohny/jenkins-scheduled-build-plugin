#!/bin/bash

# Jenkins 插件自动安装脚本
# 支持多种安装方式：CLI、REST API、文件复制

set -e

# 配置
JENKINS_URL="http://127.0.0.1:8080"
JENKINS_USER="admin"
JENKINS_PASS="admin123"
PLUGIN_FILE="target/scheduled-build.hpi"
PLUGIN_NAME="scheduled-build"
PLUGIN_VERSION="1.0.0"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 检查Jenkins是否运行
check_jenkins_running() {
    log_info "检查 Jenkins 是否运行..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$JENKINS_URL/login" > /dev/null 2>&1; then
            log_success "Jenkins 正在运行"
            return 0
        fi
        
        log_info "等待 Jenkins 启动... (尝试 $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done
    
    log_error "Jenkins 启动超时"
    return 1
}

# 检查插件是否已安装
is_plugin_installed() {
    log_info "检查插件是否已安装..."
    
    # 方法1: 检查插件文件是否存在
    local container_id=$(docker ps -q -f "name=jenkins-scheduled-build-demo-2.332")
    if [ -n "$container_id" ]; then
        if docker exec "$container_id" ls -la /var/jenkins_home/plugins/ | grep -q "scheduled-build.hpi"; then
            log_success "插件文件已存在 (通过文件系统检测)"
            return 0
        fi
    fi
    
    # 方法2: 使用REST API
    local response=$(curl -s -u "$JENKINS_USER:$JENKINS_PASS" \
        "$JENKINS_URL/pluginManager/api/json?depth=1" 2>/dev/null)
    
    if echo "$response" | grep -q "\"shortName\":\"$PLUGIN_NAME\""; then
        log_success "插件已安装 (通过REST API检测)"
        return 0
    fi
    
    # 方法3: 使用CLI
    local cli_jar="/tmp/jenkins-cli.jar"
    if [ -f "$cli_jar" ]; then
        if java -jar "$cli_jar" -s "$JENKINS_URL" -auth "$JENKINS_USER:$JENKINS_PASS" \
           list-plugins 2>/dev/null | grep -q "$PLUGIN_NAME"; then
            log_success "插件已安装 (通过CLI检测)"
            return 0
        fi
    fi
    
    log_info "插件未安装"
    return 1
}

# 下载Jenkins CLI
download_jenkins_cli() {
    local cli_jar="/tmp/jenkins-cli.jar"
    
    # 删除可能损坏的CLI文件
    if [ -f "$cli_jar" ]; then
        rm -f "$cli_jar"
    fi
    
    log_info "下载 Jenkins CLI..."
    local cli_url="$JENKINS_URL/jnlpJars/jenkins-cli.jar"
    
    # 使用更详细的下载命令
    if curl -L -s -o "$cli_jar" "$cli_url"; then
        # 验证下载的文件是否为有效的JAR文件
        if file "$cli_jar" | grep -q "Java archive"; then
            log_success "Jenkins CLI 下载成功"
            return 0
        else
            log_error "下载的文件不是有效的JAR文件"
            rm -f "$cli_jar"
            return 1
        fi
    else
        log_error "Jenkins CLI 下载失败"
        return 1
    fi
}

# 方法1: 使用CLI安装插件
install_via_cli() {
    log_info "使用 CLI 安装插件..."
    
    local cli_jar="/tmp/jenkins-cli.jar"
    
    if [ ! -f "$cli_jar" ]; then
        if ! download_jenkins_cli; then
            return 1
        fi
    fi
    
    if java -jar "$cli_jar" -s "$JENKINS_URL" -auth "$JENKINS_USER:$JENKINS_PASS" \
       install-plugin "$PLUGIN_FILE" -restart; then
        log_success "插件安装成功 (CLI方式)"
        return 0
    else
        log_error "插件安装失败 (CLI方式)"
        return 1
    fi
}

# 方法2: 使用REST API安装插件
install_via_api() {
    log_info "使用 REST API 安装插件..."
    
    # 上传插件文件
    local upload_url="$JENKINS_URL/pluginManager/uploadPlugin"
    
    if curl -s -u "$JENKINS_USER:$JENKINS_PASS" \
       -F "name=@$PLUGIN_FILE" \
       -F "mode=install" \
       "$upload_url" > /dev/null; then
        log_success "插件上传成功"
        
        # 重启Jenkins
        log_info "重启 Jenkins..."
        curl -s -u "$JENKINS_USER:$JENKINS_PASS" \
             -X POST "$JENKINS_URL/restart" > /dev/null
        
        log_success "插件安装成功 (REST API方式)"
        return 0
    else
        log_error "插件安装失败 (REST API方式)"
        return 1
    fi
}

# 方法3: 直接复制到插件目录
install_via_copy() {
    log_info "使用文件复制方式安装插件..."
    
    # 获取Jenkins容器ID
    local container_id=$(docker ps -q -f "name=jenkins-scheduled-build-demo-2.332")
    
    if [ -z "$container_id" ]; then
        log_error "未找到 Jenkins 容器"
        return 1
    fi
    
    # 复制插件文件到容器
    if docker cp "$PLUGIN_FILE" "$container_id:/var/jenkins_home/plugins/"; then
        log_success "插件文件复制成功"
        
        # 验证文件是否复制成功
        if docker exec "$container_id" ls -la /var/jenkins_home/plugins/ | grep -q "scheduled-build.hpi"; then
            log_success "插件文件验证成功"
            
            # 重启容器
            log_info "重启 Jenkins 容器..."
            docker restart "$container_id"
            
            log_success "插件安装成功 (文件复制方式)"
            return 0
        else
            log_error "插件文件复制验证失败"
            return 1
        fi
    else
        log_error "插件安装失败 (文件复制方式)"
        return 1
    fi
}

# 等待Jenkins重启完成
wait_for_restart() {
    log_info "等待 Jenkins 重启完成..."
    
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$JENKINS_URL/login" > /dev/null 2>&1; then
            log_success "Jenkins 重启完成"
            return 0
        fi
        
        log_info "等待重启... (尝试 $attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done
    
    log_error "Jenkins 重启超时"
    return 1
}

# 验证插件安装
verify_installation() {
    log_info "验证插件安装..."
    
    # 等待一段时间让插件完全加载
    sleep 10
    
    if is_plugin_installed; then
        log_success "插件安装验证成功"
        return 0
    else
        log_error "插件安装验证失败"
        return 1
    fi
}

# 主函数
main() {
    echo "=========================================="
    echo "🔌 Jenkins 插件自动安装脚本"
    echo "=========================================="
    echo ""
    
    # 检查插件文件是否存在
    if [ ! -f "$PLUGIN_FILE" ]; then
        log_error "插件文件不存在: $PLUGIN_FILE"
        echo ""
        echo "请先构建插件:"
        echo "  mvn clean package -DskipTests"
        exit 1
    fi
    
    log_info "插件文件: $PLUGIN_FILE"
    log_info "Jenkins URL: $JENKINS_URL"
    echo ""
    
    # 检查Jenkins是否运行
    if ! check_jenkins_running; then
        exit 1
    fi
    
    # 检查插件是否已安装
    if is_plugin_installed; then
        log_success "插件已安装，无需重复安装"
        exit 0
    fi
    
    echo ""
    log_info "开始安装插件..."
    echo ""
    
    # 尝试不同的安装方法
    local install_success=false
    
    # 方法1: 文件复制 (最可靠)
    if install_via_copy; then
        install_success=true
    else
        log_warning "文件复制安装失败，尝试其他方法..."
        
        # 方法2: CLI安装
        if install_via_cli; then
            install_success=true
        else
            log_warning "CLI 安装失败，尝试REST API..."
            
            # 方法3: REST API安装
            if install_via_api; then
                install_success=true
            fi
        fi
    fi
    
    if [ "$install_success" = true ]; then
        echo ""
        log_info "等待 Jenkins 重启..."
        wait_for_restart
        
        echo ""
        if verify_installation; then
            echo ""
            echo "=========================================="
            log_success "插件安装完成！"
            echo "=========================================="
            echo ""
            echo "现在可以访问 Jenkins 并使用预约构建功能："
            echo "  URL: $JENKINS_URL"
            echo "  用户名: $JENKINS_USER"
            echo "  密码: $JENKINS_PASS"
            echo ""
        else
            log_error "插件安装验证失败"
            exit 1
        fi
    else
        log_error "所有安装方法都失败了"
        exit 1
    fi
}

# 运行主函数
main "$@"