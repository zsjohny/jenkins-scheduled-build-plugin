# CI/CD 自动化指南

## 📋 概述

本项目已配置完整的 GitHub Actions CI/CD 流程，支持：
- ✅ 自动构建和测试
- ✅ PR 自动检查
- ✅ 自动发布和打包

## 🚀 GitHub Actions Workflows

### 1. CI Workflow (`ci.yml`)

**触发条件：**
- Push 到 `main`, `master`, `develop` 分支
- Pull Request 到这些分支

**执行步骤：**
1. ✅ Checkout 代码
2. ✅ 设置 JDK 11 环境
3. ✅ 验证 Maven 项目
4. ✅ 编译插件
5. ✅ 运行测试
6. ✅ 打包插件
7. ✅ 验证插件文件
8. ✅ 检查扩展点索引
9. ✅ 上传构建产物

**查看方式：**
```bash
https://github.com/zsjohny/jenkins-scheduled-build-plugin/actions
```

### 2. PR Check Workflow (`pr-check.yml`)

**触发条件：**
- Pull Request 打开、同步或重新打开时

**执行步骤：**
1. ✅ 代码格式检查
2. ✅ 编译检查
3. ✅ 依赖分析
4. ✅ 验证扩展点
5. ✅ 检查资源文件
6. ✅ 打包测试
7. ✅ 验证插件完整性
8. ✅ 生成检查总结

**PR 示例：**
当您创建 PR 时，会自动运行所有检查并在 PR 页面显示结果。

### 3. Release Workflow (`release.yml`)

**触发条件：**
- Push 带有 `v*` 前缀的 tag（如 `v1.0.0`）

**执行步骤：**
1. ✅ 构建发布版本
2. ✅ 生成 Release 说明
3. ✅ 创建 GitHub Release
4. ✅ 自动上传 .hpi 文件

## 📦 发布新版本

### 方法 1：命令行发布

```bash
# 1. 确保所有更改已提交
git add .
git commit -m "Release v1.0.0"

# 2. 创建并推送 tag
git tag -a v1.0.0 -m "Release version 1.0.0

新功能:
- 功能 A
- 功能 B

修复:
- 问题 1
- 问题 2"

git push origin v1.0.0

# 3. GitHub Actions 会自动构建并创建 Release
```

### 方法 2：GitHub 网页发布

1. 访问：https://github.com/zsjohny/jenkins-scheduled-build-plugin/releases
2. 点击 "Draft a new release"
3. 选择或创建 tag（如 `v1.0.0`）
4. 填写 Release 标题和说明
5. 点击 "Publish release"

## 🔍 查看构建状态

### 构建徽章

在 README 中添加：

```markdown
![CI](https://github.com/zsjohny/jenkins-scheduled-build-plugin/workflows/CI/badge.svg)
![Release](https://github.com/zsjohny/jenkins-scheduled-build-plugin/workflows/Release/badge.svg)
```

### 查看构建日志

1. 访问：https://github.com/zsjohny/jenkins-scheduled-build-plugin/actions
2. 点击任意 workflow 运行
3. 查看详细日志和构建产物

## 📥 下载构建产物

### 从 Actions 下载

1. 访问成功的 CI 运行
2. 在 "Artifacts" 区域下载 `scheduled-build-plugin`
3. 解压获得 `.hpi` 文件

### 从 Releases 下载

1. 访问：https://github.com/zsjohny/jenkins-scheduled-build-plugin/releases
2. 下载最新版本的 `scheduled-build.hpi`

## 🛠️ 本地测试 CI 流程

使用 [act](https://github.com/nektos/act) 在本地运行 GitHub Actions：

```bash
# 安装 act
brew install act  # macOS
# 或
curl https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash

# 运行 CI workflow
act push

# 运行 PR check
act pull_request

# 列出所有可用的 workflows
act -l
```

## 🔧 自定义 Workflows

### 修改 CI 配置

编辑 `.github/workflows/ci.yml`：

```yaml
# 例如：添加代码质量检查
- name: 代码质量检查
  run: mvn spotbugs:check
```

### 添加 Secrets

如果需要私密信息（如发布凭证）：

1. 访问：https://github.com/zsjohny/jenkins-scheduled-build-plugin/settings/secrets/actions
2. 点击 "New repository secret"
3. 添加所需的 secret
4. 在 workflow 中使用：`${{ secrets.YOUR_SECRET_NAME }}`

## 📊 工作流程图

```
┌─────────────┐
│  开发者提交   │
│   代码到 PR   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  PR Check   │
│  自动运行    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Merge PR   │
│  到 main    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  CI 构建    │
│  自动运行    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  创建 tag   │
│  (v1.0.0)   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Release    │
│  自动发布    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  .hpi 文件  │
│  可供下载    │
└─────────────┘
```

## 🎯 最佳实践

### 1. 分支策略

- `main` - 主分支，只包含稳定代码
- `develop` - 开发分支，用于日常开发
- `feature/*` - 功能分支
- `hotfix/*` - 紧急修复分支

### 2. 提交规范

使用语义化提交信息：

```
feat: 添加新功能
fix: 修复 bug
docs: 更新文档
style: 代码格式调整
refactor: 重构代码
test: 添加测试
chore: 构建/工具链更新
```

### 3. 版本号规范

遵循 [语义化版本](https://semver.org/lang/zh-CN/)：

- `v1.0.0` - 主版本.次版本.修订号
- `v1.0.0-beta.1` - 预发布版本
- `v1.0.0-rc.1` - 候选版本

### 4. Release 说明模板

```markdown
## 🎉 新功能
- 功能 A
- 功能 B

## 🐛 Bug 修复
- 修复问题 1
- 修复问题 2

## 📝 文档更新
- 更新安装指南
- 添加示例

## ⚠️ 破坏性变更
- 变更 1（如果有）

## 🔧 其他改进
- 性能优化
- 代码重构
```

## 🆘 故障排查

### CI 构建失败

1. 查看 Actions 日志
2. 检查 Java 版本是否正确（应为 11）
3. 确认 Maven 配置正确
4. 验证扩展点索引文件存在

### Release 创建失败

1. 确认 tag 格式正确（`v*`）
2. 检查是否有权限创建 Release
3. 验证 `GITHUB_TOKEN` 权限

### 构建产物缺失

1. 检查 `.gitignore` 是否正确
2. 确认 `target/` 目录在 CI 中被创建
3. 验证 Maven 打包步骤成功

## 📞 获取帮助

- GitHub Issues: https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues
- GitHub Discussions: https://github.com/zsjohny/jenkins-scheduled-build-plugin/discussions
- Actions 文档: https://docs.github.com/en/actions

## 🎉 总结

✅ **已完成的配置：**
- CI 自动构建
- PR 自动检查
- Release 自动发布
- 构建产物上传

✅ **下一步操作：**
1. 创建您的第一个 PR 测试 PR Check
2. Merge PR 后查看 CI 构建
3. 创建 tag 测试自动发布
4. 从 Releases 下载并安装插件

🚀 **Happy Coding!**

