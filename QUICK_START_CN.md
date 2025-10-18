# 快速开始指南 🚀

## 📋 目录

- [环境要求](#环境要求)
- [快速构建](#快速构建)
- [安装插件](#安装插件)
- [使用方法](#使用方法)
- [常见问题](#常见问题)

---

## 环境要求

- **Java**: 11 (必须)
- **Maven**: 3.8+
- **Jenkins**: 2.346.3+ (2022年6月及以后)

### 安装 Java 11

**macOS:**
```bash
brew install openjdk@11
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk
```

---

## 快速构建

### 方式 A: 使用构建脚本（推荐）

```bash
./build.sh
```

脚本会自动：
- ✓ 检测并设置 Java 11
- ✓ 清理旧构建
- ✓ 编译并打包插件
- ✓ 显示安装方法

### 方式 B: 手动构建

```bash
# 1. 设置 Java 11
export JAVA_HOME=$(/usr/libexec/java_home -v 11)  # macOS
# 或
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # Linux

# 2. 构建插件
mvn clean package -DskipTests

# 3. 查看生成的文件
ls -lh target/scheduled-build.hpi
```

---

## 安装插件

### 方法 1: Web 界面上传（推荐）

1. 登录 Jenkins
2. **系统管理** → **插件管理** → **高级**
3. 在 **"上传插件"** 区域
4. 点击 **"选择文件"**，选择 `target/scheduled-build.hpi`
5. 点击 **"上传"**
6. **重启 Jenkins**

### 方法 2: 命令行安装

```bash
# 复制插件到 Jenkins 插件目录
cp target/scheduled-build.hpi $JENKINS_HOME/plugins/

# 重启 Jenkins
sudo systemctl restart jenkins  # systemd
# 或
docker restart jenkins  # Docker
```

### 方法 3: Docker 演示环境

```bash
./start-jenkins.sh
```

自动启动包含演示任务的 Jenkins 环境：
- 地址: http://localhost:8080
- 用户名: admin
- 密码: admin123

---

## 使用方法

### 第 1 步：验证安装

重启 Jenkins 后：
1. 进入 **系统管理** → **插件管理** → **已安装**
2. 搜索 **"Scheduled Build Plugin"**
3. 确认状态为 **已启用** ✅

### 第 2 步：进入任务

1. 打开**任意 Jenkins 任务**（Job）页面
2. 在左侧菜单中，会**自动显示** **"预约构建"** 链接 📅
3. 点击 **"预约构建"**

> 💡 **注意**: 新版本插件自动为所有任务启用，无需配置！

### 第 3 步：添加预约

在预约构建页面：

1. **填写表单**：
   - **预约时间**: 选择未来的日期时间
   - **描述** (可选): 说明预约目的
   - **参数** (如果是参数化任务): 设置各参数值

2. **点击 "添加预约"**

3. **查看列表**: 新预约会显示在列表中，包括：
   - ⏰ 预约时间
   - ⏱️ 相对时间（如"2小时后"）
   - 📝 描述
   - 📊 状态（待执行/已执行/已取消）

### 第 4 步：管理预约

- **取消预约**: 点击 "取消" 按钮（仅待执行的预约）
- **删除记录**: 点击 "删除" 按钮（清理历史）
- **查看状态**: 实时显示预约状态

---

## 常见问题

### Q1: 构建失败 - class file major version 67

**问题**: 使用了 Java 23

**解决**:
```bash
# 设置 Java 11
export JAVA_HOME=$(/usr/libexec/java_home -v 11)  # macOS
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # Linux

# 重新构建
./build.sh
```

### Q2: 看不到"预约构建"菜单

**可能原因**:
1. ❌ 插件未正确安装
2. ❌ Jenkins 未重启
3. ❌ 浏览器缓存

**解决方法**:
1. 确认插件已安装并启用
2. **重启 Jenkins**
3. 清除浏览器缓存（Ctrl+F5 或 Cmd+Shift+R）
4. 使用隐身/无痕模式测试

### Q3: 安装插件时版本冲突

**错误**: `structs plugin version conflict`

**解决**: 使用**最新构建的插件** (2025-10-19 01:52 或更新)
- 最新版本已移除强制依赖声明
- 自动适配 Jenkins 环境中的插件版本

### Q4: 预约时间到了但未执行

**检查**:
1. 服务器时区设置
2. Jenkins 服务是否运行
3. 查看 Jenkins 日志:
   ```bash
   tail -f $JENKINS_HOME/logs/jenkins.log | grep ScheduledBuild
   ```

### Q5: 路径包含空格构建失败

**问题**: 项目在 iCloud Drive 等路径

**解决**: 已在脚本中使用 `-DskipTests` 跳过测试

---

## 📊 功能特性

### ✅ 核心功能

| 功能 | 说明 |
|------|------|
| 🔓 **自动启用** | 安装后所有任务自动支持预约构建 |
| 📅 **多任务预约** | 同一任务可设置多个预约 |
| 🎛️ **参数化支持** | 每个预约可使用不同参数值 |
| ❌ **灵活取消** | 执行前可随时取消预约 |
| 💾 **持久化存储** | 预约信息永久保存，重启后自动恢复 |
| 📊 **状态追踪** | 实时显示预约状态和倒计时 |

### 🎯 适用场景

1. **定期发版**: 周五晚上自动发布生产环境
2. **夜间测试**: 凌晨执行完整回归测试
3. **临时任务**: 预约数据迁移、清理等操作
4. **错峰执行**: 避开高峰期执行耗时任务

---

## 🔗 相关文档

- **[README.md](README.md)** - 完整文档
- **[INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)** - 详细安装指南
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - 故障排查
- **[COMPATIBILITY.md](COMPATIBILITY.md)** - 兼容性说明
- **[RELEASE_NOTES.md](RELEASE_NOTES.md)** - 版本更新

---

## 🎬 视频演示

### 基本使用流程

```
1. 进入任务页面
   ↓
2. 点击左侧 "预约构建" 链接
   ↓
3. 填写预约时间和描述
   ↓
4. (可选) 设置构建参数
   ↓
5. 点击 "添加预约"
   ↓
6. 等待自动执行！
```

---

## 💡 最佳实践

### 1. 合理设置预约时间

- ✅ 错开高峰期
- ✅ 预留足够时间（至少提前 5 分钟）
- ✅ 考虑时区差异

### 2. 添加清晰的描述

```
✅ 好的描述: "v1.2.3 生产环境发版 - Jira-1234"
❌ 不好的描述: "测试"
```

### 3. 定期清理历史记录

- 删除已执行/已取消的旧预约
- 保持列表整洁

### 4. 备份重要预约信息

预约数据存储在:
```
$JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
```

---

## 🚀 立即开始

```bash
# 1. 克隆项目
git clone https://github.com/your-org/jenkins-scheduled-build-plugin.git
cd jenkins-scheduled-build-plugin

# 2. 构建插件
./build.sh

# 3. 上传到 Jenkins
# 浏览器访问 Jenkins → 系统管理 → 插件管理 → 高级 → 上传插件

# 4. 重启 Jenkins

# 5. 开始使用！
# 进入任意任务 → 点击 "预约构建"
```

---

**祝您使用愉快！** 🎉

有问题？查看 [故障排查指南](TROUBLESHOOTING.md) 或提交 GitHub Issue。

