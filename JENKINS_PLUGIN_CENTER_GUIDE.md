# Jenkins 插件中心托管指南

本文档说明如何将 Scheduled Build Plugin 提交到 Jenkins 官方插件中心，使其在 Jenkins 插件管理页面可下载。

## 📋 前置条件

### 1. 必需账号
- ✅ GitHub 账号（已有）
- ✅ Jenkins JIRA 账号
- ✅ Jenkins Artifactory 账号（用于发布）

### 2. 代码要求
- ✅ 插件已完成开发和测试
- ✅ 符合 Jenkins 插件开发规范
- ✅ 有完整的文档（README）
- ✅ 有开源许可证（MIT）

---

## 🚀 托管流程

### 步骤 1: 创建 Jenkins 账号

#### 1.1 注册 Jira 账号
访问：https://accounts.jenkins.io/
- 使用 GitHub 账号登录或创建新账号
- 这个账号将用于：
  - Jenkins JIRA（问题跟踪）
  - Jenkins Artifactory（插件发布）
  - Jenkins Wiki（文档）

### 步骤 2: 申请托管到 jenkinsci 组织

#### 2.1 创建托管申请
访问：https://github.com/jenkins-infra/repository-permissions-updater

**方式 A：通过 JIRA（推荐）**

1. 访问 Jenkins JIRA: https://issues.jenkins.io/
2. 创建新问题：
   - Project: HOSTING
   - Issue Type: New Plugin Hosting Request
   - Summary: Host scheduled-build-plugin
   - Description: 参考下面的模板

**JIRA 问题模板**:
```
Plugin Name: Scheduled Build Plugin
GitHub Repository: https://github.com/zsjohny/jenkins-scheduled-build-plugin
Short Description: 支持多条预约构建任务，可以设置参数化构建并在到期前取消

I have:
- [x] Read the plugin hosting documentation
- [x] Confirmed the plugin name is available
- [x] Set up proper licensing (MIT)
- [x] Configured proper SCM information in pom.xml
- [x] Added myself as a developer in pom.xml

Plugin Details:
- Version: 1.0.0
- Jenkins Version: 2.401.3+
- Java Version: 11+
- License: MIT

GitHub Username: zsjohny
Email: your-email@example.com

I would like to host this plugin in the jenkinsci organization.
```

**方式 B：通过 Pull Request**

1. Fork https://github.com/jenkins-infra/repository-permissions-updater
2. 创建文件 `permissions/plugin-scheduled-build.yml`:

```yaml
---
name: "scheduled-build"
github: "jenkinsci/scheduled-build-plugin"
paths:
- "io/jenkins/plugins/scheduled-build"
developers:
- "zsjohny"
```

3. 提交 Pull Request

### 步骤 3: 转移仓库到 jenkinsci

一旦托管申请被批准：

1. **接受邀请**
   - 你会收到加入 jenkinsci 组织的邀请
   - 在 GitHub 通知中接受邀请

2. **转移仓库**
   ```bash
   # 在 GitHub 网页操作：
   # Settings → Transfer ownership → jenkinsci
   ```
   
   或者 Jenkins 团队会帮你创建新仓库并要求你推送代码

3. **更新本地仓库**
   ```bash
   cd jenkins-scheduled-build-plugin
   git remote set-url origin git@github.com:jenkinsci/scheduled-build-plugin.git
   git push -u origin main --tags
   ```

### 步骤 4: 配置 CD 发布

#### 4.1 添加 Jenkinsfile

创建 `Jenkinsfile` 用于自动发布：

```groovy
#!/usr/bin/env groovy

buildPlugin(
  useContainerAgent: true,
  configurations: [
    [platform: 'linux', jdk: 11],
    [platform: 'windows', jdk: 11],
  ]
)
```

#### 4.2 配置发布凭证

Jenkins 官方 CI 会自动配置发布凭证，无需手动设置。

### 步骤 5: 发布到插件中心

#### 5.1 准备发布

```bash
# 确保版本号正确
vim pom.xml  # 确认版本号，如 1.0.0

# 提交所有更改
git add .
git commit -m "chore: prepare for plugin center release"
git push
```

#### 5.2 创建发布

```bash
# 使用 Maven 插件的 release 流程
mvn release:prepare release:perform

# 或者手动打标签
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

#### 5.3 等待发布

- Jenkins CI 会自动构建并发布到 Artifactory
- 通常需要几小时到一天时间
- 插件会出现在更新中心

---

## 📝 更新 pom.xml（当前状态）

当前 `pom.xml` 已包含必要的元数据：

```xml
✅ <url>https://github.com/jenkinsci/scheduled-build-plugin</url>
✅ <licenses> - MIT License
✅ <developers> - 开发者信息
✅ <scm> - 源码管理信息
```

### 需要更新的地方

1. **开发者邮箱**
   ```xml
   <email>your-email@example.com</email>
   ```
   替换为你的真实邮箱

2. **确认版本号**
   ```xml
   <version>1.0.0</version>
   ```

---

## 🔍 验证清单

在提交托管申请前，确保：

- [ ] pom.xml 包含完整的元数据
  - [ ] 名称和描述
  - [ ] 许可证信息
  - [ ] 开发者信息（真实邮箱）
  - [ ] SCM 信息
  - [ ] 正确的 Jenkins 版本要求

- [ ] README.md 完整
  - [ ] 插件功能说明
  - [ ] 安装方法
  - [ ] 使用示例
  - [ ] 截图（可选但推荐）

- [ ] 代码质量
  - [ ] 无明显的 bug
  - [ ] 有基本的测试（推荐）
  - [ ] 遵循 Java 编码规范

- [ ] 文档
  - [ ] LICENSE 文件
  - [ ] CHANGELOG.md
  - [ ] CONTRIBUTING.md（可选）

---

## 🎯 发布后

### 插件中心更新时间

- **首次发布**: 4-24 小时
- **后续更新**: 2-8 小时

### 验证插件可用性

1. **在更新中心搜索**
   ```
   Jenkins → 系统管理 → 插件管理 → 可选插件
   搜索: "Scheduled Build"
   ```

2. **检查插件页面**
   ```
   https://plugins.jenkins.io/scheduled-build/
   ```

3. **Maven Central**
   ```
   https://repo.jenkins-ci.org/releases/io/jenkins/plugins/scheduled-build/
   ```

### 更新插件

```bash
# 1. 修改代码
# 2. 更新版本号
vim pom.xml  # 1.0.0 → 1.1.0

# 3. 提交更改
git add .
git commit -m "feat: new feature"
git push

# 4. 创建 release
git tag -a v1.1.0 -m "Release v1.1.0"
git push origin v1.1.0

# Jenkins CI 会自动发布
```

---

## 📚 参考文档

### 官方文档
- [插件托管指南](https://www.jenkins.io/doc/developer/publishing/requesting-hosting/)
- [插件发布流程](https://www.jenkins.io/doc/developer/publishing/releasing/)
- [插件开发教程](https://www.jenkins.io/doc/developer/tutorial/)

### 相关链接
- [Jenkins JIRA](https://issues.jenkins.io/)
- [Jenkins Artifactory](https://repo.jenkins-ci.org/)
- [Repository Permissions Updater](https://github.com/jenkins-infra/repository-permissions-updater)
- [Jenkins 插件中心](https://plugins.jenkins.io/)

---

## 🔧 当前仓库状态

- **当前位置**: `https://github.com/zsjohny/jenkins-scheduled-build-plugin`
- **目标位置**: `https://github.com/jenkinsci/scheduled-build-plugin`
- **托管状态**: ⏳ 待申请

---

## 💡 快速开始托管流程

### 1 分钟快速操作

```bash
# 1. 更新开发者邮箱（替换 your-email@example.com）
vim pom.xml

# 2. 提交更改
git add pom.xml
git commit -m "chore: update developer email for plugin center"
git push

# 3. 访问 Jenkins JIRA 创建托管申请
# https://issues.jenkins.io/
# Project: HOSTING
# Issue Type: New Plugin Hosting Request
```

---

## ❓ 常见问题

### Q: 托管申请需要多久？
A: 通常 1-3 个工作日，取决于审核队列。

### Q: 可以保留在个人账号下吗？
A: 不可以。插件中心只接受 jenkinsci 组织下的插件。

### Q: 发布后可以撤回吗？
A: 已发布的版本不能删除，但可以发布新版本修复问题。

### Q: 需要付费吗？
A: 完全免费，Jenkins 插件托管是开源项目。

### Q: 谁来审核申请？
A: Jenkins 基础设施团队会审核托管申请。

---

**准备好了吗？** 按照上面的步骤，开始将你的插件提交到 Jenkins 插件中心吧！🚀

