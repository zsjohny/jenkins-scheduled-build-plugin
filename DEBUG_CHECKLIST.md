# 🔍 预约构建插件 - 调试检查清单

## 问题：左侧菜单看不到"预约构建"链接

### ✅ 检查清单

按照以下步骤逐一检查：

---

## 第 1 步：确认插件已安装

### 检查方法：
1. 登录 Jenkins
2. 进入 **系统管理** → **插件管理** → **已安装**
3. 在搜索框输入 **"Scheduled Build"** 或 **"scheduled-build"**

### 预期结果：
- ✅ 找到 **Scheduled Build Plugin**
- ✅ 状态显示为 **已启用**（没有警告图标）
- ✅ 版本信息显示（如 1.0.0-SNAPSHOT）

### 如果没找到：
```bash
# 重新上传插件
# 系统管理 → 插件管理 → 高级 → 上传插件
# 选择 target/scheduled-build.hpi
```

---

## 第 2 步：确认 Jenkins 已重启

### ⚠️ 关键：插件安装后必须重启 Jenkins

### 重启方法：

**方法 A：安全重启（推荐）**
1. 浏览器访问：`http://your-jenkins/safeRestart`
2. 或：系统管理 → 安全重启

**方法 B：命令行重启**
```bash
# Docker
docker restart jenkins

# Systemd
sudo systemctl restart jenkins

# 直接运行的 Jenkins
# 停止当前进程，然后重新启动
```

### 验证重启：
- 重新登录 Jenkins
- 检查右下角版本信息，确认时间已更新

---

## 第 3 步：清除浏览器缓存

### 操作方法：

**快捷键：**
- Windows/Linux: `Ctrl + F5` 或 `Ctrl + Shift + R`
- Mac: `Cmd + Shift + R`

**或者：**
1. 打开浏览器设置
2. 清除缓存和Cookie
3. 重新登录 Jenkins

**测试方法：**
- 使用浏览器隐身/无痕模式访问
- 如果隐身模式能看到，说明是缓存问题

---

## 第 4 步：检查 Jenkins 日志

### 查看日志：

**方法 A：Web 界面**
1. 系统管理 → 系统日志 → 所有日志
2. 搜索 **"ScheduledBuild"** 或 **"scheduled-build"**

**方法 B：命令行**
```bash
# 查看 Jenkins 日志
tail -f $JENKINS_HOME/logs/jenkins.log | grep -i scheduled

# 或者
docker logs -f jenkins | grep -i scheduled
```

### 应该看到的日志：
```
INFO: Loading plugin: scheduled-build
INFO: Extension ScheduledBuildActionFactory registered
```

### 如果看到错误：
- 记录错误信息
- 检查是否有版本冲突
- 查看是否有依赖问题

---

## 第 5 步：启用调试日志

### 开启详细日志：

1. **系统管理** → **系统日志** → **日志记录器**
2. 点击 **添加新记录器**
3. 配置：
   - 名称：`io.jenkins.plugins.scheduledbuild`
   - 日志级别：`ALL` 或 `FINE`
4. 保存

### 重新加载页面查看日志：
```bash
# 实时查看日志
tail -f $JENKINS_HOME/logs/all.log | grep ScheduledBuild
```

### 应该看到：
```
FINE: 添加预约构建功能到任务: scheduled-build-demo
```

---

## 第 6 步：检查插件文件

### 验证插件文件：

```bash
# 检查插件是否存在
ls -l $JENKINS_HOME/plugins/scheduled-build.*

# 应该看到：
# scheduled-build.hpi (或 .jpi)
# scheduled-build/ (解压后的目录)
```

### 查看插件元数据：
```bash
# 查看 MANIFEST.MF
unzip -p $JENKINS_HOME/plugins/scheduled-build.hpi META-INF/MANIFEST.MF | grep -E "Plugin-Version|Jenkins-Version"
```

### 预期输出：
```
Plugin-Version: 1.0.0-SNAPSHOT (private-10/19/2025 01:59-Johny)
Jenkins-Version: 2.346.3
```

---

## 第 7 步：验证扩展点注册

### 检查扩展点：

在 Jenkins Script Console 执行（系统管理 → 脚本命令行）：

```groovy
// 检查 TransientActionFactory 扩展点
import jenkins.model.*
import hudson.model.*

def factories = Jenkins.instance.getExtensionList(jenkins.model.TransientActionFactory.class)
factories.each { factory ->
    println "Factory: ${factory.class.name}"
    if (factory.class.name.contains("ScheduledBuild")) {
        println "  ✅ 找到预约构建扩展点！"
        println "  Type: ${factory.type()}"
    }
}

// 检查具体任务的 Actions
def job = Jenkins.instance.getItem("scheduled-build-demo")
if (job) {
    println "\n任务: ${job.name}"
    job.getAllActions().each { action ->
        println "  - ${action.class.name}"
        if (action.class.name.contains("ScheduledBuild")) {
            println "    ✅ 找到预约构建 Action！"
            println "    显示名称: ${action.displayName}"
            println "    URL: ${action.urlName}"
            println "    图标: ${action.iconFileName}"
        }
    }
}
```

### 预期输出：
```
Factory: io.jenkins.plugins.scheduledbuild.ScheduledBuildAction$ScheduledBuildActionFactory
  ✅ 找到预约构建扩展点！
  Type: interface hudson.model.Job

任务: scheduled-build-demo
  - io.jenkins.plugins.scheduledbuild.ScheduledBuildAction
    ✅ 找到预约构建 Action！
    显示名称: 预约构建
    URL: scheduled-builds
    图标: notepad.png
```

---

## 第 8 步：检查任务类型

### 验证任务类型兼容性：

```groovy
// 在 Jenkins Script Console 中执行
def job = Jenkins.instance.getItem("scheduled-build-demo")
println "任务类型: ${job.class.name}"
println "是否是 Job: ${job instanceof hudson.model.Job}"
```

### 支持的任务类型：
- ✅ Freestyle Project
- ✅ Pipeline
- ✅ Multi-configuration Project
- ✅ Maven Project

---

## 第 9 步：检查权限

### 验证当前用户权限：

```groovy
// 检查当前用户权限
import jenkins.model.*
import hudson.security.*

def user = User.current()
def job = Jenkins.instance.getItem("scheduled-build-demo")

println "当前用户: ${user?.id ?: 'anonymous'}"
if (job) {
    println "READ 权限: ${job.hasPermission(hudson.model.Item.READ)}"
    println "BUILD 权限: ${job.hasPermission(hudson.model.Item.BUILD)}"
}
```

### 需要的权限：
- ✅ `READ` - 查看预约列表
- ✅ `BUILD` - 添加/取消预约

---

## 🔧 常见问题解决

### 问题 A：插件安装但未生效

**解决方法：**
```bash
# 1. 完全停止 Jenkins
sudo systemctl stop jenkins

# 2. 删除旧的插件缓存
rm -rf $JENKINS_HOME/plugins/scheduled-build.hpi.pinned
rm -rf $JENKINS_HOME/plugins/scheduled-build

# 3. 重新复制插件
cp target/scheduled-build.hpi $JENKINS_HOME/plugins/

# 4. 重新启动
sudo systemctl start jenkins
```

### 问题 B：Jenkins 版本太旧

检查 Jenkins 版本：
```groovy
println Jenkins.instance.version
```

最低要求：**2.346.3**

### 问题 C：插件冲突

检查是否有其他插件冲突：
```bash
# 查看加载失败的插件
cat $JENKINS_HOME/logs/jenkins.log | grep -i "failed to load plugin"
```

---

## 📞 获取帮助

如果以上步骤都无法解决问题：

### 收集诊断信息：

```bash
# 1. Jenkins 版本
curl -s http://localhost:8080/api/json | grep -o '"version":"[^"]*"'

# 2. 插件列表
curl -s http://localhost:8080/pluginManager/api/json?depth=1 | grep scheduled

# 3. 最新日志
tail -100 $JENKINS_HOME/logs/jenkins.log

# 4. 插件清单
unzip -p $JENKINS_HOME/plugins/scheduled-build.hpi META-INF/MANIFEST.MF
```

### 提交 Issue 时包含：
1. Jenkins 版本
2. 操作系统
3. 插件版本
4. 完整错误日志
5. Script Console 输出
6. 插件列表

---

## 🎯 快速测试脚本

创建文件 `test-plugin.sh`:

```bash
#!/bin/bash

echo "=== 预约构建插件诊断脚本 ==="
echo ""

# 1. 检查插件文件
echo "1. 检查插件文件..."
if [ -f "$JENKINS_HOME/plugins/scheduled-build.hpi" ]; then
    echo "✅ 插件文件存在"
    ls -lh "$JENKINS_HOME/plugins/scheduled-build.hpi"
else
    echo "❌ 插件文件不存在"
fi

# 2. 检查插件目录
echo ""
echo "2. 检查插件目录..."
if [ -d "$JENKINS_HOME/plugins/scheduled-build" ]; then
    echo "✅ 插件已解压"
else
    echo "❌ 插件未解压"
fi

# 3. 检查日志
echo ""
echo "3. 最近的相关日志..."
tail -50 "$JENKINS_HOME/logs/jenkins.log" | grep -i scheduled || echo "没有找到相关日志"

echo ""
echo "=== 诊断完成 ==="
```

运行：
```bash
chmod +x test-plugin.sh
./test-plugin.sh
```

---

**最后更新**: 2025-10-19

