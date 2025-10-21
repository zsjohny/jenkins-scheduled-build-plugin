# 🚀 提交到 Jenkins 插件中心 - 快速操作指南

本指南帮助你快速完成插件中心托管申请，使插件可以在 Jenkins 插件管理页面直接搜索安装。

---

## ⚡ 快速开始（5 分钟）

### 步骤 1: 更新开发者邮箱（1 分钟）

编辑 `pom.xml` 第 35 行，替换为你的真实邮箱：

```xml
<developers>
    <developer>
        <id>zsjohny</id>
        <name>Johny</name>
        <email>your-email@example.com</email>  <!-- 改成真实邮箱 -->
    </developer>
</developers>
```

提交更改：
```bash
git add pom.xml
git commit -m "chore: update developer email"
git push
```

### 步骤 2: 注册 Jenkins 账号（2 分钟）

访问：https://accounts.jenkins.io/

- 使用 GitHub 账号登录即可
- 这个账号将用于 JIRA 和 Artifactory

### 步骤 3: 创建托管申请（2 分钟）

**方式 A: 通过 JIRA（推荐）**

1. 访问：https://issues.jenkins.io/secure/CreateIssue.jspa
2. 填写表单：
   - **Project**: `HOSTING`
   - **Issue Type**: `New Plugin Hosting Request`
   - **Summary**: `Host scheduled-build-plugin`
   - **Description**: 复制下面的内容

```
Plugin Name: Scheduled Build Plugin
Repository: https://github.com/zsjohny/jenkins-scheduled-build-plugin
Description: Jenkins 预约构建插件 - 支持多条预约构建任务，可以设置参数化构建并在到期前取消

I have completed the following:
✅ Read the plugin hosting documentation
✅ Confirmed the plugin name "scheduled-build" is available
✅ Set up proper licensing (MIT)
✅ Configured SCM information in pom.xml
✅ Added developer information in pom.xml
✅ Created proper documentation (README, CHANGELOG)

Plugin Details:
- Version: 1.0.0
- Jenkins Version Required: 2.401.3 LTS+
- Java Version: 11+
- License: MIT
- Main Features:
  * Multiple scheduled builds per job
  * Parameterized build support
  * Cancel scheduled builds before execution
  * Persistent storage
  * Web-based management interface

GitHub Username: zsjohny
Email: [你的邮箱]

Repository URL: https://github.com/zsjohny/jenkins-scheduled-build-plugin
Target URL: https://github.com/jenkinsci/scheduled-build-plugin

I would like to host this plugin in the jenkinsci organization.
```

3. 点击 **Create** 创建问题

**方式 B: 通过 Pull Request**

1. Fork: https://github.com/jenkins-infra/repository-permissions-updater

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

---

## 📋 等待审批

### 通常时间线

- **申请提交**: 即时
- **审核批准**: 1-3 个工作日
- **仓库转移**: 1 天内
- **首次发布**: 转移后即可
- **出现在插件中心**: 4-24 小时

### 审批后操作

#### 1. 接受组织邀请

你会收到邮件邀请加入 `jenkinsci` 组织：
- 在 GitHub 通知中接受邀请
- 或访问：https://github.com/jenkinsci

#### 2. 转移仓库

**在 GitHub 网页操作：**
1. 进入仓库：https://github.com/zsjohny/jenkins-scheduled-build-plugin
2. Settings → Scroll down → Transfer ownership
3. New owner: `jenkinsci`
4. Repository name: `scheduled-build-plugin`
5. 确认转移

**更新本地仓库：**
```bash
cd jenkins-scheduled-build-plugin
git remote set-url origin git@github.com:jenkinsci/scheduled-build-plugin.git
git fetch --all
git push -u origin main --tags
```

#### 3. 验证转移成功

```bash
git remote -v
# 应该显示:
# origin  git@github.com:jenkinsci/scheduled-build-plugin.git (fetch)
# origin  git@github.com:jenkinsci/scheduled-build-plugin.git (push)
```

---

## 🎯 发布到插件中心

### 自动发布（推荐）

仓库转移后，Jenkins 官方 CI 会自动配置：

1. **创建新版本**
   ```bash
   # 更新版本号（如果需要）
   vim pom.xml  # 例如改为 1.0.1
   
   git add pom.xml
   git commit -m "chore: bump version to 1.0.1"
   git push
   ```

2. **打标签发布**
   ```bash
   git tag -a v1.0.1 -m "Release v1.0.1"
   git push origin v1.0.1
   ```

3. **等待构建**
   - Jenkins CI 自动构建
   - 自动发布到 Artifactory
   - 2-8 小时后出现在插件中心

### 验证发布成功

#### 检查 Artifactory
```
https://repo.jenkins-ci.org/releases/io/jenkins/plugins/scheduled-build/
```

#### 检查插件中心
```
https://plugins.jenkins.io/scheduled-build/
```

#### 在 Jenkins 中搜索
```
系统管理 → 插件管理 → 可选插件
搜索: "Scheduled Build"
```

---

## 📊 当前状态检查

运行以下命令检查准备情况：

```bash
# 检查 pom.xml
grep -A 5 "<developers>" pom.xml
grep "<url>" pom.xml
grep "<scm>" pom.xml

# 检查文件
ls -la Jenkinsfile README.md LICENSE

# 检查 Git 状态
git status
git log --oneline -5
git tag -l
```

**期望输出：**
- ✅ `pom.xml` 包含开发者信息
- ✅ `pom.xml` 包含 SCM 信息
- ✅ 存在 `Jenkinsfile`
- ✅ 存在 `README.md`
- ✅ 存在 `LICENSE`
- ✅ Git 仓库干净（无未提交更改）
- ✅ 已有 v1.0.0 标签

---

## ❓ 常见问题

### Q1: 必须转移仓库到 jenkinsci 吗？
**A**: 是的，Jenkins 插件中心只接受 `jenkinsci` 组织下的插件。

### Q2: 转移后我还能访问吗？
**A**: 可以！你会成为 `jenkinsci/scheduled-build-plugin` 的维护者，拥有完整权限。

### Q3: 如何更新已发布的插件？
**A**: 更新代码，修改版本号，打新标签推送即可。Jenkins CI 会自动发布。

### Q4: 发布需要付费吗？
**A**: 完全免费！Jenkins 插件托管是开源项目。

### Q5: 申请被拒怎么办？
**A**: 根据反馈修改，通常是文档或代码质量问题。改进后重新申请。

### Q6: 可以先测试发布流程吗？
**A**: 转移后可以发布 SNAPSHOT 版本测试，不会出现在插件中心。

---

## 📚 详细文档

如需更多信息，请查看：

- 📖 [完整托管指南](JENKINS_PLUGIN_CENTER_GUIDE.md) - 详细流程和技术细节
- 📖 [Jenkins 官方文档](https://www.jenkins.io/doc/developer/publishing/requesting-hosting/)
- 📖 [发布流程](https://www.jenkins.io/doc/developer/publishing/releasing/)

---

## 🎉 成功后

插件发布到中心后，用户可以：

### 在 Jenkins 中安装

```
系统管理 → 插件管理 → 可选插件
搜索: "Scheduled Build"
点击安装 → 重启 Jenkins
```

### 插件页面

```
https://plugins.jenkins.io/scheduled-build/
```

包含：
- 下载统计
- 版本历史
- 文档链接
- GitHub 链接

---

## ✅ 行动清单

完成以下步骤：

- [ ] 步骤 1: 更新 `pom.xml` 开发者邮箱
- [ ] 步骤 2: 注册 Jenkins 账号（https://accounts.jenkins.io/）
- [ ] 步骤 3: 创建 JIRA 托管申请（https://issues.jenkins.io/）
- [ ] 步骤 4: 等待审批（1-3 工作日）
- [ ] 步骤 5: 接受 jenkinsci 组织邀请
- [ ] 步骤 6: 转移仓库到 jenkinsci
- [ ] 步骤 7: 验证自动发布配置
- [ ] 步骤 8: 等待出现在插件中心（4-24 小时）

---

**准备好了吗？** 从步骤 1 开始，5 分钟完成申请！🚀

有问题？查看 [JENKINS_PLUGIN_CENTER_GUIDE.md](JENKINS_PLUGIN_CENTER_GUIDE.md) 或在 JIRA 问题中提问。

