# 🔧 脚本工具目录

本目录包含 Jenkins 预约构建插件的开发和测试脚本。

## 📜 脚本列表

### 构建脚本

#### build.sh
**用途**: 构建插件 HPI 文件

**使用方法**:
```bash
./scripts/build.sh
```

**功能**:
- 自动设置 Java 11 环境
- 执行 Maven 构建（跳过测试）
- 生成 `target/scheduled-build.hpi`

**输出**:
- `target/scheduled-build.hpi` - 插件安装包

---

### Docker 环境脚本

#### start-jenkins.sh
**用途**: 启动 Jenkins Docker 演示环境

**使用方法**:
```bash
./scripts/start-jenkins.sh
```

**功能**:
- 检查 Docker 是否运行
- 自动构建插件（如果不存在）
- 启动 Jenkins 容器
- 提供访问地址和登录信息

**访问地址**: http://localhost:8080

---

#### update-plugin.sh
**用途**: 更新已运行的 Jenkins 容器中的插件

**使用方法**:
```bash
./scripts/update-plugin.sh
```

**功能**:
- 重新构建插件
- 复制到容器的 plugins 目录
- 重启 Jenkins 容器

**适用场景**: 开发调试时快速更新插件

---

#### reinstall-plugin.sh
**用途**: 完全重装插件（清理旧数据）

**使用方法**:
```bash
./scripts/reinstall-plugin.sh
```

**功能**:
- 停止并删除容器
- 清理数据卷
- 重新构建和安装

**适用场景**: 需要全新环境测试

---

### 验证脚本（Groovy）

#### verify-installation.groovy
**用途**: 验证插件安装和扩展点注册

**使用方法**:
1. 打开 Jenkins Script Console
2. 粘贴脚本内容
3. 点击运行

**检查项**:
- ✅ ScheduledBuildManager 是否初始化
- ✅ TransientActionFactory 是否注册
- ✅ 任务 Action 是否添加

**输出示例**:
```
=== 🎯 最终验证 ===
Jenkins 版本: 2.401.3
✅ ScheduledBuildManager: 已初始化
🎉 ScheduledBuildActionFactory - 自动注册成功！
```

---

#### verify-timezone.groovy
**用途**: 验证 Jenkins 时区配置

**使用方法**:
1. 打开 Jenkins Script Console
2. 粘贴脚本内容
3. 点击运行

**检查项**:
- ⏰ 系统时区设置
- 🕐 当前时间
- 🌍 时区偏移
- ✅ 是否为中国时区

**输出示例**:
```
=== 🌍 Jenkins 时区配置验证 ===
系统时区: Asia/Shanghai
当前时间: Sat Oct 20 12:00:00 CST 2025
UTC Offset: +8 小时
✅ 当前使用中国时区 (CST/UTC+8)
```

---

## 🚀 快速使用指南

### 首次使用

1. **构建插件**
   ```bash
   ./scripts/build.sh
   ```

2. **启动测试环境**
   ```bash
   ./scripts/start-jenkins.sh
   ```

3. **访问 Jenkins**
   ```
   http://localhost:8080
   用户名: admin
   密码: admin123
   ```

4. **验证安装**
   - 在 Jenkins Script Console 运行 `verify-installation.groovy`

### 开发调试

**修改代码后更新插件**:
```bash
./scripts/update-plugin.sh
```

**需要全新环境**:
```bash
./scripts/reinstall-plugin.sh
```

---

## 📝 脚本依赖

### 系统要求
- **macOS/Linux**: 所有脚本
- **Docker**: start-jenkins.sh, update-plugin.sh, reinstall-plugin.sh
- **Java 11**: build.sh
- **Maven 3.8+**: build.sh

### 环境检查

脚本会自动检查：
- ✅ Docker 是否运行
- ✅ Java 11 是否安装
- ✅ Maven 是否可用

如果缺少依赖，脚本会给出提示。

---

## 🔧 脚本特性

### 自动化
- 自动检测操作系统（macOS/Linux）
- 自动设置 Java 11 环境
- 自动构建缺失的插件

### 用户友好
- 彩色输出（✅ ❌ ⚠️）
- 详细的进度提示
- 错误时给出解决建议

### 安全
- 使用 `-e` 标志（遇错即停）
- 检查命令执行结果
- 清晰的错误信息

---

## 💡 使用技巧

### 1. 快速迭代开发

```bash
# 终端1: 监视日志
docker logs -f jenkins-scheduled-build-demo

# 终端2: 修改代码后快速更新
./scripts/update-plugin.sh
```

### 2. 调试时区问题

```bash
# 启动 Jenkins
./scripts/start-jenkins.sh

# 在 Script Console 运行
./scripts/verify-timezone.groovy
```

### 3. 测试不同 Jenkins 版本

修改 `docker-compose.yml`:
```yaml
image: jenkins/jenkins:2.332.2-jdk11  # 或其他版本
```

然后重新安装:
```bash
./scripts/reinstall-plugin.sh
```

---

## 🐛 故障排查

### 脚本无法执行

```bash
# 添加执行权限
chmod +x scripts/*.sh
```

### Java 版本错误

```bash
# macOS: 安装 Java 11
brew install openjdk@11

# 手动设置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

### Docker 容器无法启动

```bash
# 查看日志
docker logs jenkins-scheduled-build-demo

# 清理并重新开始
docker-compose down -v
./scripts/start-jenkins.sh
```

---

## 📚 相关文档

- [开发指南](../docs/CONTRIBUTING.md)
- [构建指南](../docs/QUICKSTART.md)
- [故障排查](../docs/TROUBLESHOOTING.md)

---

**返回**: [项目主页](../README.md) | [文档目录](../docs/)

