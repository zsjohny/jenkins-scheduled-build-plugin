# 故障排查指南

## 安装问题

### 问题 1: structs 插件版本冲突

**错误信息:**
```
java.io.IOException: Failed to load: Structs Plugin (structs 338.v848422169819)
- Jenkins (2.414.3) or higher required
```

**原因:**
- 插件声明了特定版本的 structs 依赖
- 您的 Jenkins 版本需要更高版本的 structs
- 版本冲突导致安装失败

**✅ 解决方案:**
已在最新版本中修复！新版本不再强制指定 structs 版本，会自动使用 Jenkins 环境中已有的版本。

**操作步骤:**
1. 下载最新构建的插件：`target/scheduled-build.hpi`
2. 上传到 Jenkins 并重启
3. 问题应该解决

---

### 问题 2: 安装后看不到预约构建菜单

**症状:**
- 插件已安装并启用
- 但任务页面没有"预约构建"链接

**可能原因:**
1. 使用了旧版本插件（需要手动配置）
2. Jenkins 未重启
3. 浏览器缓存问题
4. 权限问题

**✅ 解决方案:**

1. **确认使用最新版本**
   ```bash
   # 检查插件版本（在 Jenkins 日志中）
   # 应该显示: Plugin-Version: 1.0.0-SNAPSHOT (private-10/19/2025 01:48-Johny)
   ```

2. **重启 Jenkins**
   - 系统管理 → 安全重启
   - 或使用命令: `sudo systemctl restart jenkins`

3. **清除浏览器缓存**
   - 按 `Ctrl + F5` (Windows) 或 `Cmd + Shift + R` (Mac)
   - 或使用隐私/无痕模式

4. **检查权限**
   - 确保有任务的 `READ` 权限（查看）
   - 确保有任务的 `BUILD` 权限（添加预约）

---

### 问题 3: Jenkins 版本不兼容

**错误信息:**
```
This version of maven-hpi-plugin requires Jenkins 2.361 or later
```

**原因:**
- Jenkins 版本低于最低要求

**✅ 解决方案:**

**选项 A: 升级 Jenkins（推荐）**
```bash
# 备份数据
tar -czf jenkins-backup.tar.gz $JENKINS_HOME

# 升级到最新 LTS 版本
# 根据您的安装方式选择对应的升级方法
```

**选项 B: 降级插件配置**
如果无法升级 Jenkins，需要修改 `pom.xml`:
```xml
<properties>
    <jenkins.version>YOUR_JENKINS_VERSION</jenkins.version>
</properties>
<parent>
    <version>COMPATIBLE_VERSION</version>
</parent>
```
然后重新构建。

---

### 问题 4: 预约时间到了但没有执行

**症状:**
- 预约状态仍显示"待执行"
- 超过预约时间但未触发构建

**可能原因:**
1. Jenkins 服务器时区不正确
2. Jenkins 在预约时间停机
3. 任务被禁用
4. 系统资源不足

**✅ 排查步骤:**

1. **检查系统时间和时区**
   ```bash
   # 查看服务器时间
   date
   
   # 查看 Jenkins 时区设置
   # 系统管理 → 系统配置 → 时区
   ```

2. **查看 Jenkins 日志**
   ```bash
   # 查看日志文件
   tail -f $JENKINS_HOME/logs/jenkins.log
   
   # 搜索预约相关日志
   grep "ScheduledBuildManager" $JENKINS_HOME/logs/jenkins.log
   ```

3. **检查任务状态**
   - 确认任务未被禁用
   - 确认任务配置正确

4. **验证预约数据**
   ```bash
   # 查看预约数据文件
   cat $JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
   ```

---

### 问题 5: 构建失败 - 路径包含空格

**错误信息:**
```
The forked VM terminated without properly saying goodbye
```

**原因:**
- 项目路径包含空格（如 iCloud Drive）
- Maven 测试执行失败

**✅ 解决方案:**

**方案 A: 跳过测试（推荐）**
```bash
mvn clean package -DskipTests
```

**方案 B: 移动项目到无空格路径**
```bash
# 移动项目
mv "/Users/Johny/Library/Mobile Documents/..." ~/github/jenkins-scheduled-build-plugin
cd ~/github/jenkins-scheduled-build-plugin

# 正常构建
mvn clean package
```

---

### 问题 6: Java 版本不兼容

**错误信息:**
```
Unsupported class file major version 67
```

**原因:**
- Maven 使用了 Java 23，但项目需要 Java 11

**✅ 解决方案:**

```bash
# 设置 Java 11 (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# 验证 Java 版本
java -version
mvn -version

# 重新构建
mvn clean package -DskipTests
```

**Linux 系统:**
```bash
# 使用 update-alternatives
sudo update-alternatives --config java

# 或直接设置 JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

---

## 使用问题

### 问题 7: 无法取消预约

**症状:**
- 点击"取消"按钮无效
- 或显示错误信息

**可能原因:**
1. 预约已经执行
2. 预约已经过期
3. 权限不足

**✅ 解决方案:**
- 只能取消状态为"待执行"的预约
- 确保有任务的 `BUILD` 权限
- 如果预约已过期，使用"删除"按钮删除记录

---

### 问题 8: 参数化构建参数未生效

**症状:**
- 设置了参数但构建时使用的是默认值

**可能原因:**
1. 参数名称拼写错误
2. 任务未配置为参数化构建
3. 参数类型不匹配

**✅ 解决方案:**
1. 检查任务配置中的参数定义
2. 确保参数名称完全匹配（区分大小写）
3. 查看构建日志确认参数值

---

### 问题 9: 预约数据丢失

**症状:**
- Jenkins 重启后预约列表为空

**可能原因:**
1. 数据文件损坏
2. 权限问题
3. 磁盘空间不足

**✅ 解决方案:**

1. **检查数据文件**
   ```bash
   ls -l $JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
   ```

2. **恢复备份**（如果有）
   ```bash
   cp $JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml.backup \
      $JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
   ```

3. **检查权限**
   ```bash
   # 确保 Jenkins 用户有读写权限
   chown jenkins:jenkins $JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
   ```

---

## 调试技巧

### 启用调试日志

1. **通过 Web 界面**
   - 系统管理 → 系统日志 → 添加新记录器
   - 记录器: `io.jenkins.plugins.scheduledbuild`
   - 日志级别: `ALL` 或 `FINE`

2. **查看详细日志**
   ```bash
   tail -f $JENKINS_HOME/logs/all.log | grep ScheduledBuild
   ```

### 验证插件加载

```bash
# 检查插件是否正确加载
grep "ScheduledBuildPlugin" $JENKINS_HOME/logs/jenkins.log

# 查看插件元数据
cat $JENKINS_HOME/plugins/scheduled-build.hpi.pinned
```

### 测试预约功能

1. 创建一个测试任务
2. 设置一个 1 分钟后的预约
3. 观察日志输出
4. 验证是否正确触发

---

## 获取帮助

如果以上方法都无法解决问题，请：

1. **收集信息**
   - Jenkins 版本
   - 插件版本
   - Java 版本
   - 错误日志
   - 预约配置截图

2. **提交 Issue**
   - GitHub Issue: [项目地址]
   - 包含完整的错误堆栈
   - 描述重现步骤

3. **查看文档**
   - [安装指南](INSTALLATION_GUIDE.md)
   - [兼容性说明](COMPATIBILITY.md)
   - [发布说明](RELEASE_NOTES.md)

---

## 常见警告（可忽略）

以下警告可以安全忽略：

```
WARNING: Parameter 'showDeprecation' is unknown for plugin 'maven-hpi-plugin'
```
这是构建时的警告，不影响插件功能。

```
WARNING: Failed to build parent project
```
这是 Maven 的警告，不影响插件构建。

---

**更新时间**: 2025-10-19

