# Jenkins 2.332.2 LTS 支持分支

## 📋 分支说明

此分支 `support-jenkins-2.332.2` 专门支持 **Jenkins 2.332.2 LTS** 版本（2022年4月发布）。

### 为什么需要这个分支？

- **主分支**（main）支持 Jenkins 2.401.3+ LTS (2023年8月)
- **此分支**支持 Jenkins 2.332.2 LTS (2022年4月)
- 适用于无法升级到最新 Jenkins 版本的环境

---

## 🔧 技术调整

### 1. POM 配置变更

| 配置项 | 主分支 (main) | 此分支 (support-jenkins-2.332.2) |
|--------|---------------|----------------------------------|
| Jenkins 版本 | 2.401.3 | 2.332.2 |
| 父 POM 版本 | 4.75 | 4.40 |
| 插件版本 | 1.0.0 | 1.0.0-jenkins-2.332.2 |
| Docker 镜像 | jenkins:2.401.3-jdk11 | jenkins:2.332.2-jdk11 |

### 2. 兼容性保证

本插件使用 **双重扩展点注册机制**，确保在 Jenkins 2.332.2 上正常工作：

1. **META-INF/services/hudson.Extension** 索引文件
   - 旧版本 Jenkins 可能不完全支持

2. **PluginImpl + @Initializer** 注解
   - 在 Jenkins 启动时主动注册扩展点
   - 确保功能正常加载

---

## 📦 安装使用

### 1. 下载插件

从此分支的 Release 下载 `scheduled-build.hpi` 文件：
```
https://github.com/zsjohny/jenkins-scheduled-build-plugin/releases
```

查找标记为 `jenkins-2.332.2` 的版本。

### 2. 安装到 Jenkins

```bash
# 方式 1: Web 界面
系统管理 → 插件管理 → 高级 → 上传插件

# 方式 2: 命令行
cp scheduled-build.hpi $JENKINS_HOME/plugins/
# 重启 Jenkins
```

### 3. 验证安装

在 Jenkins Script Console 运行：

```groovy
println "Jenkins 版本: " + Jenkins.getVersion()
println "插件版本: " + Jenkins.instance.pluginManager.getPlugin("scheduled-build")?.version

// 验证扩展点
def factories = Jenkins.get().getExtensionList(jenkins.model.TransientActionFactory.class)
println "\nTransientActionFactory 数量: " + factories.size()
factories.each { factory ->
    if (factory.class.name.contains("ScheduledBuild")) {
        println "✅ " + factory.class.simpleName + " - 已注册"
    }
}
```

**期望输出：**
```
Jenkins 版本: 2.332.2
插件版本: 1.0.0-jenkins-2.332.2
TransientActionFactory 数量: X
✅ ScheduledBuildActionFactory - 已注册
```

---

## 🚀 本地构建测试

### 1. 构建插件

```bash
# 确保在 support-jenkins-2.332.2 分支
git checkout support-jenkins-2.332.2

# 使用 Java 11 构建
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
mvn clean package -DskipTests

# 查看生成的文件
ls -lh target/scheduled-build.hpi
```

### 2. 启动测试环境

```bash
# 使用 Docker Compose 启动 Jenkins 2.332.2
docker-compose up -d

# 查看日志
docker logs -f jenkins-scheduled-build-demo-2.332

# 访问 Jenkins
open http://localhost:8080
```

### 3. 手动安装插件

```bash
# 复制插件到容器
docker cp target/scheduled-build.hpi jenkins-scheduled-build-demo-2.332:/var/jenkins_home/plugins/

# 重启容器
docker restart jenkins-scheduled-build-demo-2.332
```

---

## ✅ 功能验证清单

在 Jenkins 2.332.2 上验证以下功能：

### 基础功能
- [ ] 插件安装成功
- [ ] 任务页面显示"预约构建"链接
- [ ] 可以添加预约
- [ ] 预约列表正常显示

### 核心功能
- [ ] 预约到期自动触发构建
- [ ] 参数化构建支持
- [ ] 取消预约功能
- [ ] Jenkins 重启后预约恢复

### 扩展点注册
- [ ] ScheduledBuildManager 已初始化
- [ ] ScheduledBuildActionFactory 已注册
- [ ] TransientAction 正常工作

---

## 🐛 已知问题和限制

### 1. 功能限制

由于 Jenkins 2.332.2 版本较旧，可能存在以下限制：

- ⚠️ 部分新版本 Jenkins API 不可用
- ⚠️ 某些 UI 组件可能不完全兼容
- ⚠️ 性能可能略低于新版本

### 2. 依赖冲突

如果遇到依赖冲突：

```xml
<!-- 在 pom.xml 中可能需要排除某些依赖 -->
<dependency>
    <groupId>...</groupId>
    <artifactId>...</artifactId>
    <exclusions>
        <exclusion>
            <groupId>conflicting-group</groupId>
            <artifactId>conflicting-artifact</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 3. 测试覆盖

建议在部署前进行充分测试：
- 单元测试
- 集成测试
- 手动功能测试

---

## 📝 开发说明

### 代码同步

此分支定期从主分支同步代码：

```bash
# 更新此分支
git checkout support-jenkins-2.332.2
git merge main

# 解决冲突（如果有）
# 主要是 pom.xml 和 docker-compose.yml

# 提交更改
git commit -m "merge: sync from main branch"
git push origin support-jenkins-2.332.2
```

### 版本号规范

此分支的版本号格式：
```
主版本.次版本.修订版-jenkins-最低版本

例如：
1.0.0-jenkins-2.332.2
1.1.0-jenkins-2.332.2
```

### 发布流程

1. **测试**
   ```bash
   mvn clean test
   mvn clean package -DskipTests
   ```

2. **Docker 验证**
   ```bash
   docker-compose up -d
   # 测试所有功能
   ```

3. **创建标签**
   ```bash
   git tag -a v1.0.0-jenkins-2.332.2 -m "Release for Jenkins 2.332.2 LTS"
   git push origin v1.0.0-jenkins-2.332.2
   ```

4. **GitHub Release**
   - 创建 Release
   - 上传 `scheduled-build.hpi`
   - 说明此版本支持 Jenkins 2.332.2

---

## 🔗 相关资源

### Jenkins 2.332.2 LTS 信息
- [Jenkins 2.332.2 发布说明](https://www.jenkins.io/changelog-stable/#v2.332.2)
- [Jenkins 2.332.2 下载](https://www.jenkins.io/download/lts/2.332.2/)
- [Jenkins LTS 升级指南](https://www.jenkins.io/doc/upgrade-guide/)

### 插件开发文档
- [Jenkins 插件开发指南](https://www.jenkins.io/doc/developer/plugin-development/)
- [Jenkins 插件父 POM 版本](https://github.com/jenkinsci/plugin-pom)
- [Jenkins 核心 API](https://javadoc.jenkins.io/)

---

## ❓ 常见问题

### Q1: 为什么不在主分支直接支持 2.332.2？

**A**: 主分支使用较新的父 POM (4.75) 和 API，降低版本可能影响新功能开发。独立分支更灵活。

### Q2: 这个分支会长期维护吗？

**A**: 是的，此分支会持续维护，直到 Jenkins 2.332.2 LTS 不再广泛使用。

### Q3: 可以从此分支升级到主分支版本吗？

**A**: 可以！只需升级 Jenkins 到 2.401.3+，然后安装主分支的插件版本。

### Q4: 遇到问题如何报告？

**A**: 在 GitHub Issues 中报告，标注 `jenkins-2.332.2` 标签。

---

## 📧 支持

如有问题：
- 📝 [提交 Issue](https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues)
- 📖 查看主分支 [README](https://github.com/zsjohny/jenkins-scheduled-build-plugin)
- 📧 联系开发者：zs.johny@163.com

---

**分支状态**: 🟢 活跃维护中

**最后更新**: 2025-10-19

