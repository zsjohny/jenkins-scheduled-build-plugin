# 🔄 完整的重新安装步骤

## 问题现象

插件已上传并重启，但扩展点仍未加载：
- ❌ 扩展点列表中没有 ScheduledBuildActionFactory
- ❌ 任务页面没有"预约构建"链接

## 🧹 完整清理和重装流程

### 第 1 步：停止 Jenkins

```bash
# Docker
docker stop jenkins

# Systemd
sudo systemctl stop jenkins
```

### 第 2 步：完全删除旧插件

在 Jenkins 服务器上执行：

```bash
# 设置 JENKINS_HOME 路径
export JENKINS_HOME=/var/jenkins_home  # Docker
# 或
export JENKINS_HOME=/var/lib/jenkins  # Linux
# 或
export JENKINS_HOME=~/jenkins_home  # 自定义

# 删除所有相关文件
rm -f $JENKINS_HOME/plugins/scheduled-build.hpi
rm -f $JENKINS_HOME/plugins/scheduled-build.jpi
rm -rf $JENKINS_HOME/plugins/scheduled-build
rm -f $JENKINS_HOME/plugins/scheduled-build.hpi.pinned
rm -f $JENKINS_HOME/plugins/scheduled-build.jpi.pinned

# 验证已删除
ls -la $JENKINS_HOME/plugins/ | grep scheduled
```

**预期结果**: 没有任何输出

### 第 3 步：复制新插件

```bash
# 复制最新构建的插件
cp target/scheduled-build.hpi $JENKINS_HOME/plugins/

# 确认文件存在
ls -lh $JENKINS_HOME/plugins/scheduled-build.hpi
```

### 第 4 步：启动 Jenkins

```bash
# Docker
docker start jenkins

# Systemd
sudo systemctl start jenkins
```

### 第 5 步：等待完全启动

```bash
# 查看日志，等待启动完成
tail -f $JENKINS_HOME/logs/jenkins.log

# 等待看到：
# "Jenkins is fully up and running"
```

### 第 6 步：验证插件加载

在 **系统管理** → **脚本命令行** 中执行：

```groovy
// 完整的诊断脚本
import jenkins.model.*
import hudson.model.*
import hudson.PluginWrapper

println "=" * 60
println "预约构建插件完整诊断"
println "=" * 60
println ""

// 1. 检查插件是否安装
println "【1】检查插件安装状态:"
def pluginManager = Jenkins.instance.pluginManager
def plugin = pluginManager.getPlugin("scheduled-build")
if (plugin) {
    println "  ✅ 插件已找到"
    println "  插件名称: ${plugin.shortName}"
    println "  版本: ${plugin.version}"
    println "  是否启用: ${plugin.isEnabled()}"
    println "  是否活动: ${plugin.isActive()}"
    if (!plugin.isActive()) {
        println "  ⚠️  插件未激活！"
    }
} else {
    println "  ❌ 插件未找到！"
    println "  → 请检查插件文件是否存在"
    return
}
println ""

// 2. 检查扩展点注册
println "【2】检查扩展点注册:"
def factories = Jenkins.instance.getExtensionList(jenkins.model.TransientActionFactory.class)
println "  共找到 ${factories.size()} 个 TransientActionFactory"

def found = false
factories.each { factory ->
    def className = factory.class.name
    if (className.contains("ScheduledBuild")) {
        println "  ✅ 找到预约构建扩展点！"
        println "     类名: ${className}"
        println "     目标类型: ${factory.type()}"
        found = true
    }
}

if (!found) {
    println "  ❌ 未找到 ScheduledBuildActionFactory"
    println "  → 扩展点索引文件可能缺失或未被加载"
}
println ""

// 3. 检查具体任务
println "【3】检查任务 Actions:"
def job = Jenkins.instance.getItem("scheduled-build-demo")
if (job) {
    println "  任务: ${job.name}"
    println "  类型: ${job.class.simpleName}"
    
    def actions = job.getAllActions()
    println "  Actions 数量: ${actions.size()}"
    
    actions.each { action ->
        def name = action.class.simpleName
        println "    - ${name}"
        if (name.contains("ScheduledBuild")) {
            println "      ✅ 预约构建 Action 已添加！"
            println "      显示名称: ${action.displayName}"
            println "      URL: ${action.urlName}"  
            println "      图标: ${action.iconFileName}"
        }
    }
    
    // 检查是否应该有 ScheduledBuildAction
    if (!actions.any { it.class.simpleName.contains("ScheduledBuild") }) {
        println "  ❌ 未找到 ScheduledBuildAction"
        println "  → 扩展点工厂未正常工作"
    }
} else {
    println "  ❌ 未找到任务 'scheduled-build-demo'"
}
println ""

// 4. 检查类加载
println "【4】检查类加载:"
try {
    def actionClass = Class.forName(
        "io.jenkins.plugins.scheduledbuild.ScheduledBuildAction",
        false,
        plugin.classLoader
    )
    println "  ✅ ScheduledBuildAction 类已加载"
    
    def factoryClass = Class.forName(
        "io.jenkins.plugins.scheduledbuild.ScheduledBuildAction\$ScheduledBuildActionFactory",
        false,
        plugin.classLoader
    )
    println "  ✅ ScheduledBuildActionFactory 类已加载"
    
    // 检查注解
    def annotations = factoryClass.getAnnotations()
    println "  类注解: ${annotations.collect { it.annotationType().simpleName }}"
    if (annotations.any { it.annotationType().name == "hudson.Extension" }) {
        println "  ✅ @Extension 注解存在"
    } else {
        println "  ❌ @Extension 注解缺失！"
    }
    
} catch (Exception e) {
    println "  ❌ 类加载失败: ${e.message}"
}
println ""

// 5. 系统信息
println "【5】系统信息:"
println "  Jenkins 版本: ${Jenkins.instance.version}"
println "  Java 版本: ${System.getProperty('java.version')}"
println ""

println "=" * 60
println "诊断完成"
println "=" * 60
```

---

## 🔍 如果仍然失败

### 方案 A：检查 Jenkins 日志

```bash
# 搜索相关错误
grep -i "scheduled-build\|ScheduledBuild" $JENKINS_HOME/logs/jenkins.log | tail -50

# 搜索扩展点加载错误
grep -i "extension\|failed to load" $JENKINS_HOME/logs/jenkins.log | tail -30
```

### 方案 B：手动触发插件重新加载

在脚本命令行执行：

```groovy
// 重新加载插件
import jenkins.model.Jenkins

def pluginManager = Jenkins.instance.pluginManager
def plugin = pluginManager.getPlugin("scheduled-build")

if (plugin) {
    println "尝试重新加载插件..."
    // 注意：这可能需要重启才能生效
    pluginManager.dynamicLoad(new File(Jenkins.instance.rootDir, "plugins/scheduled-build.hpi"))
    println "请重启 Jenkins 使更改生效"
} else {
    println "插件未找到"
}
```

### 方案 C：验证插件文件完整性

```bash
# 解压并检查
cd /tmp
unzip -q $JENKINS_HOME/plugins/scheduled-build.hpi
ls -la WEB-INF/lib/

# 检查 JAR 文件
jar tf WEB-INF/lib/scheduled-build.jar | grep "META-INF/services"
jar xf WEB-INF/lib/scheduled-build.jar META-INF/services/hudson.Extension
cat META-INF/services/hudson.Extension

# 应该看到：
# io.jenkins.plugins.scheduledbuild.ScheduledBuildManager
# io.jenkins.plugins.scheduledbuild.ScheduledBuildAction$ScheduledBuildActionFactory
# io.jenkins.plugins.scheduledbuild.ScheduledBuildProperty$ScheduledBuildPropertyDescriptor

# 清理
cd -
rm -rf /tmp/WEB-INF /tmp/META-INF
```

---

## 🎯 预期结果

完成以上步骤后，诊断脚本应该输出：

```
====================================================
预约构建插件完整诊断
====================================================

【1】检查插件安装状态:
  ✅ 插件已找到
  插件名称: scheduled-build
  版本: 1.0.0-SNAPSHOT
  是否启用: true
  是否活动: true

【2】检查扩展点注册:
  共找到 X 个 TransientActionFactory
  ✅ 找到预约构建扩展点！
     类名: io.jenkins.plugins.scheduledbuild.ScheduledBuildAction$ScheduledBuildActionFactory
     目标类型: interface hudson.model.Job

【3】检查任务 Actions:
  任务: scheduled-build-demo
  类型: FreeStyleProject
  Actions 数量: 3
    - ParametersDefinitionProperty
    - RenameAction
    - ScheduledBuildAction
      ✅ 预约构建 Action 已添加！
      显示名称: 预约构建
      URL: scheduled-builds
      图标: notepad.png

【4】检查类加载:
  ✅ ScheduledBuildAction 类已加载
  ✅ ScheduledBuildActionFactory 类已加载
  类注解: [Extension]
  ✅ @Extension 注解存在

【5】系统信息:
  Jenkins 版本: 2.361.4
  Java 版本: 11.0.x

====================================================
诊断完成
====================================================
```

---

## 📞 如果还是不行

请提供以下信息：

1. 完整的诊断脚本输出
2. Jenkins 日志中的相关错误（如果有）
3. 插件文件验证结果
4. Jenkins 版本和 Java 版本

我会根据具体情况提供进一步的解决方案。

---

**最后更新**: 2025-10-19 02:10

