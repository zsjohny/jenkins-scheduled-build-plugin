# 📁 项目目录组织说明

## 📊 重组概述

**日期**: 2025-10-21  
**目的**: 清理根目录，建立清晰的文档和脚本组织结构

---

## 🗂️ 新的目录结构

```
jenkins-scheduled-build-plugin/
│
├── 📄 README.md                  # 项目主页（唯一保留在根目录的文档）
├── 📄 LICENSE                    # 开源许可证
├── 📄 pom.xml                   # Maven 配置文件
├── 📄 docker-compose.yml        # Docker 测试环境配置
├── 📄 Jenkinsfile               # CI/CD 流水线定义
├── 📄 .gitignore                # Git 忽略规则
│
├── 📚 docs/                      # 所有文档 (17个)
│   ├── README.md                # 文档索引和导航
│   ├── QUICKSTART.md            # 快速开始指南
│   ├── INSTALLATION_GUIDE.md    # 详细安装指南
│   ├── USAGE_CN.md              # 完整使用说明
│   ├── ARCHITECTURE.md          # 架构设计文档
│   ├── CONTRIBUTING.md          # 贡献指南
│   ├── CHANGELOG.md             # 变更日志
│   ├── VERSION_REQUIREMENTS.md  # 版本要求
│   ├── TROUBLESHOOTING.md       # 故障排查
│   ├── CI_CD_GUIDE.md           # CI/CD 配置指南
│   ├── RELEASE_NOTES.md         # 发布说明
│   ├── RELEASE_CHECKLIST.md     # 发布检查清单
│   ├── JENKINS_PLUGIN_CENTER_GUIDE.md  # 插件中心指南
│   ├── SUBMIT_TO_PLUGIN_CENTER.md      # 插件提交指南
│   ├── NEXT_STEPS.md            # 下一步计划
│   ├── README_PREVIEW.md        # README 预览
│   ├── PIPELINE_JOB_FIX.md      # Pipeline 任务兼容性修复 (仅 main 分支)
│   ├── SERIALIZATION_FIX.md     # 序列化问题修复 (仅 main 分支)
│   ├── UI_IMPROVEMENTS.md       # UI 改进说明 (仅 main 分支)
│   ├── JENKINS_2.332.2_SUPPORT.md  # Jenkins 2.332.2 支持 (仅 support 分支)
│   └── PROJECT_TREE.txt         # 项目文件树
│
├── 🔧 scripts/                   # 所有脚本工具 (7个)
│   ├── README.md                # 脚本使用说明
│   ├── build.sh                 # 构建插件 HPI
│   ├── start-jenkins.sh         # 启动 Docker 测试环境
│   ├── update-plugin.sh         # 热更新插件（开发调试）
│   ├── reinstall-plugin.sh      # 完全重装插件（清理环境）
│   ├── verify-installation.groovy    # 验证插件安装
│   └── verify-timezone.groovy   # 验证时区配置
│
├── 🔌 init-scripts/              # Jenkins 初始化脚本
│   ├── 01-create-admin-user.groovy   # 创建管理员账户
│   └── 02-create-demo-job.groovy     # 创建演示任务
│
├── 💻 src/                       # 源代码
│   └── main/
│       ├── java/                # Java 源代码
│       │   └── io/jenkins/plugins/scheduledbuild/
│       │       ├── ScheduledBuildTask.java
│       │       ├── ScheduledBuildManager.java
│       │       ├── ScheduledBuildProperty.java
│       │       ├── ScheduledBuildAction.java
│       │       └── PluginImpl.java
│       └── resources/           # 资源文件
│           ├── index.jelly
│           └── io/jenkins/plugins/scheduledbuild/
│               ├── Messages.properties
│               ├── Messages_zh_CN.properties
│               └── ...
│
├── 🚀 .github/                   # GitHub 配置
│   └── workflows/               # GitHub Actions
│       ├── ci.yml               # 持续集成
│       ├── pr-check.yml         # PR 自动检查
│       └── release.yml          # 自动发布
│
└── 📦 target/                    # 构建输出（.gitignore）
    └── scheduled-build.hpi      # 插件安装包
```

---

## 🎯 重组目标

### ✅ 达成的目标

1. **根目录简洁**
   - 仅保留必要的配置文件
   - 仅保留一个主 README.md
   - 清晰的目录结构，一眼可见

2. **文档集中管理**
   - 所有文档在 `docs/` 目录
   - 提供 `docs/README.md` 索引
   - 分类清晰（用户文档、开发文档、发布文档等）

3. **脚本集中管理**
   - 所有脚本在 `scripts/` 目录
   - 提供 `scripts/README.md` 使用说明
   - 功能明确（构建、测试、部署、验证）

4. **易于导航**
   - 每个目录都有 README.md
   - 主 README.md 提供快速导航链接
   - 文档之间相互链接

5. **维护性提升**
   - 文件组织有序，易于查找
   - 新增文档有明确的存放位置
   - Git 历史清晰（使用 git mv 保留历史）

---

## 📝 目录说明

### docs/ - 文档目录

**用途**: 存放所有项目文档

**分类**:
- **用户文档**: QUICKSTART.md, INSTALLATION_GUIDE.md, USAGE_CN.md
- **开发文档**: ARCHITECTURE.md, CONTRIBUTING.md
- **版本文档**: CHANGELOG.md, VERSION_REQUIREMENTS.md
- **发布文档**: RELEASE_NOTES.md, RELEASE_CHECKLIST.md
- **运维文档**: TROUBLESHOOTING.md, CI_CD_GUIDE.md
- **技术说明**: PIPELINE_JOB_FIX.md, SERIALIZATION_FIX.md 等

**文件数量**: 17-20 个（根据分支不同）

---

### scripts/ - 脚本目录

**用途**: 存放开发和测试脚本

**分类**:
- **构建脚本**: build.sh
- **环境脚本**: start-jenkins.sh, update-plugin.sh, reinstall-plugin.sh
- **验证脚本**: verify-installation.groovy, verify-timezone.groovy

**特点**:
- 所有脚本都有详细注释
- 提供错误检查和用户提示
- 支持 macOS 和 Linux

**文件数量**: 7 个

---

### init-scripts/ - Jenkins 初始化

**用途**: Docker 环境启动时自动执行的 Groovy 脚本

**说明**: 这些脚本在 Jenkins 容器启动时自动运行，用于：
- 创建管理员账户（admin/admin123）
- 创建演示任务（scheduled-build-demo）

**文件数量**: 2 个

---

### .github/ - GitHub 集成

**用途**: GitHub 特定的配置和自动化

**包含**:
- `workflows/` - GitHub Actions CI/CD 流水线
- 未来可能添加: ISSUE_TEMPLATE, PULL_REQUEST_TEMPLATE 等

**文件数量**: 3 个 workflow

---

## 🔄 迁移映射

### 文档迁移

| 旧位置（根目录） | 新位置（docs/） | 说明 |
|-----------------|----------------|------|
| ARCHITECTURE.md | docs/ARCHITECTURE.md | 架构文档 |
| CHANGELOG.md | docs/CHANGELOG.md | 变更日志 |
| CI_CD_GUIDE.md | docs/CI_CD_GUIDE.md | CI/CD 指南 |
| CONTRIBUTING.md | docs/CONTRIBUTING.md | 贡献指南 |
| INSTALLATION_GUIDE.md | docs/INSTALLATION_GUIDE.md | 安装指南 |
| JENKINS_2.332.2_SUPPORT.md | docs/JENKINS_2.332.2_SUPPORT.md | 2.332.2 支持 |
| JENKINS_PLUGIN_CENTER_GUIDE.md | docs/JENKINS_PLUGIN_CENTER_GUIDE.md | 插件中心指南 |
| NEXT_STEPS.md | docs/NEXT_STEPS.md | 下一步计划 |
| PIPELINE_JOB_FIX.md | docs/PIPELINE_JOB_FIX.md | Pipeline 修复 |
| PROJECT_TREE.txt | docs/PROJECT_TREE.txt | 文件树 |
| QUICKSTART.md | docs/QUICKSTART.md | 快速开始 |
| README_PREVIEW.md | docs/README_PREVIEW.md | README 预览 |
| RELEASE_CHECKLIST.md | docs/RELEASE_CHECKLIST.md | 发布检查 |
| RELEASE_NOTES.md | docs/RELEASE_NOTES.md | 发布说明 |
| SERIALIZATION_FIX.md | docs/SERIALIZATION_FIX.md | 序列化修复 |
| SUBMIT_TO_PLUGIN_CENTER.md | docs/SUBMIT_TO_PLUGIN_CENTER.md | 提交指南 |
| TROUBLESHOOTING.md | docs/TROUBLESHOOTING.md | 故障排查 |
| UI_IMPROVEMENTS.md | docs/UI_IMPROVEMENTS.md | UI 改进 |
| USAGE_CN.md | docs/USAGE_CN.md | 使用说明 |
| VERSION_REQUIREMENTS.md | docs/VERSION_REQUIREMENTS.md | 版本要求 |

### 脚本迁移

| 旧位置（根目录） | 新位置（scripts/） | 说明 |
|-----------------|-------------------|------|
| build.sh | scripts/build.sh | 构建脚本 |
| start-jenkins.sh | scripts/start-jenkins.sh | 启动环境 |
| update-plugin.sh | scripts/update-plugin.sh | 更新插件 |
| reinstall-plugin.sh | scripts/reinstall-plugin.sh | 重装插件 |
| verify-installation.groovy | scripts/verify-installation.groovy | 验证安装 |
| verify-timezone.groovy | scripts/verify-timezone.groovy | 验证时区 |

---

## 💡 使用指南

### 查找文档

1. **从根目录开始**: 阅读 `README.md`
2. **查看文档索引**: 打开 `docs/README.md`
3. **直接访问**: 文档都在 `docs/` 目录下

**示例**:
```bash
# 快速开始
cat docs/QUICKSTART.md

# 查看所有文档
ls docs/

# 搜索文档
grep -r "预约构建" docs/
```

### 使用脚本

1. **查看脚本说明**: 打开 `scripts/README.md`
2. **执行脚本**: `./scripts/脚本名.sh`

**示例**:
```bash
# 构建插件
./scripts/build.sh

# 启动测试环境
./scripts/start-jenkins.sh

# 更新插件（开发时）
./scripts/update-plugin.sh
```

### 贡献文档

**添加新文档**:
1. 在 `docs/` 目录创建文档
2. 更新 `docs/README.md` 索引
3. 在主 `README.md` 中添加链接（如果是重要文档）

**添加新脚本**:
1. 在 `scripts/` 目录创建脚本
2. 添加执行权限: `chmod +x scripts/xxx.sh`
3. 更新 `scripts/README.md` 说明

---

## 📊 重组统计

### 文件移动

- **文档移动**: 17-20 个
- **脚本移动**: 6 个
- **新增文件**: 3 个（2个 README.md + 1个 PROJECT_ORGANIZATION.md）
- **删除文件**: 0 个（使用 git mv 保留历史）

### 提交记录

- **主提交**: `refactor: 重组项目目录结构`
- **补充提交**: `refactor: 移动剩余文档到 docs 目录`
- **影响分支**: main, support-jenkins-2.332.2

### 根目录清洁度

**重组前**: 40+ 个文件和目录  
**重组后**: 16 个文件和目录 ✅

---

## 🎉 重组成果

### Before 重组

```
jenkins-scheduled-build-plugin/
├── README.md
├── ARCHITECTURE.md
├── CHANGELOG.md
├── CI_CD_GUIDE.md
├── CONTRIBUTING.md
├── ... (15+ 个 .md 文件)
├── build.sh
├── start-jenkins.sh
├── ... (6 个 .sh/.groovy 文件)
├── pom.xml
├── docker-compose.yml
└── ... (其他配置文件)
```
**问题**: 根目录混乱，难以找到文件

### After 重组

```
jenkins-scheduled-build-plugin/
├── README.md              # 唯一的主文档
├── pom.xml               # 配置文件
├── docker-compose.yml
├── Jenkinsfile
├── LICENSE
├── .gitignore
├── docs/                 # 📚 所有文档
├── scripts/              # 🔧 所有脚本
├── src/                  # 💻 源代码
├── init-scripts/         # 🔌 初始化
├── .github/              # 🚀 CI/CD
└── target/               # 📦 构建输出
```
**优势**: 结构清晰，易于导航

---

## 🔗 快速链接

- **[项目主页](../README.md)**
- **[文档索引](README.md)**
- **[脚本说明](../scripts/README.md)**
- **[贡献指南](CONTRIBUTING.md)**
- **[快速开始](QUICKSTART.md)**

---

## 📋 维护建议

### 添加新文件时

1. **文档** → `docs/` 目录
2. **脚本** → `scripts/` 目录
3. **配置** → 根目录
4. **源代码** → `src/` 目录

### 更新索引

添加新文档或脚本后，记得更新：
- `docs/README.md` - 文档索引
- `scripts/README.md` - 脚本说明
- 主 `README.md` - 如果是重要文件

### Git 操作

**移动文件**:
```bash
# 使用 git mv 保留历史
git mv oldpath/file.md docs/file.md
```

**不要直接**:
```bash
# ❌ 这会丢失 Git 历史
mv oldpath/file.md docs/file.md
```

---

**最后更新**: 2025-10-21  
**维护者**: @zsjohny  
**状态**: ✅ 已完成

