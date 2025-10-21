# Jenkins 预约构建插件 (Scheduled Build Plugin)

[![GitHub Release](https://img.shields.io/github/v/release/zsjohny/jenkins-scheduled-build-plugin)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/releases)
[![GitHub Stars](https://img.shields.io/github/stars/zsjohny/jenkins-scheduled-build-plugin?style=social)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/zsjohny/jenkins-scheduled-build-plugin?style=social)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/network/members)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.401.3%2B-blue)](https://www.jenkins.io/)
[![GitHub Issues](https://img.shields.io/github/issues/zsjohny/jenkins-scheduled-build-plugin)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues)
[![GitHub Downloads](https://img.shields.io/github/downloads/zsjohny/jenkins-scheduled-build-plugin/total)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/releases)

## 简介

这是一个Jenkins插件，为任务提供预约构建功能。可以设置多条定时构建任务，每个任务支持不同的参数配置，并且可以在到期前取消预约。

> **📦 插件中心托管**: 正在申请提交到 [Jenkins 插件中心](https://plugins.jenkins.io/)，届时可直接在 Jenkins 插件管理中搜索安装。详见 [JENKINS_PLUGIN_CENTER_GUIDE.md](JENKINS_PLUGIN_CENTER_GUIDE.md)

## 主要功能

- ✅ **多任务预约**：支持为同一个Jenkins任务设置多条预约构建
- ✅ **参数化构建**：每个预约可以配置不同的构建参数
- ✅ **灵活取消**：未到期的预约可以随时取消
- ✅ **持久化存储**：预约信息持久化保存，Jenkins重启后自动恢复
- ✅ **直观界面**：提供友好的Web界面管理所有预约
- ✅ **状态追踪**：实时查看预约状态（待执行、已执行、已取消、已过期）

## 安装要求

- **Jenkins**: **2.401.3 LTS 或更高版本** （推荐使用 2.401.3+ LTS）
- **Java**: 11 或更高版本
- **插件依赖**: 无额外依赖，使用Jenkins自带核心插件

### 技术实现

本插件使用 **双重注册机制** 确保在所有 Jenkins 版本中都能正常工作：

1. **扩展点索引文件** (`META-INF/services/hudson.Extension`)
   - Jenkins 2.401.3+ 支持自动读取

2. **插件初始化器** (`PluginImpl.java`)
   - 使用 `@Initializer` 注解在插件加载时自动注册扩展点
   - 检测索引文件是否已被读取，如未读取则手动注册
   - **无需任何外部 Groovy 脚本**
   - 纯 Java 实现，标准 Jenkins 插件机制

这种设计确保：
- ✅ 在支持扩展点索引的新版本 Jenkins 中自动注册
- ✅ 在不支持的旧版本中通过 Initializer 注册
- ✅ 标准的 Jenkins 插件部署流程，无需额外配置
- ✅ 开箱即用，安装后立即可用

详细的技术实现说明请参考 [VERSION_REQUIREMENTS.md](VERSION_REQUIREMENTS.md)

## 构建插件

### 1. 克隆项目

```bash
git clone https://github.com/zsjohny/jenkins-scheduled-build-plugin.git
cd jenkins-scheduled-build-plugin
```

### 2. 使用Maven构建

```bash
mvn clean package
```

构建成功后，会在 `target` 目录下生成 `scheduled-build.hpi` 文件。

### 3. 安装到Jenkins

有以下几种安装方式：

#### 方式一：通过Jenkins管理界面上传
1. 登录Jenkins
2. 进入 "系统管理" → "插件管理" → "高级"
3. 在"上传插件"区域，选择生成的 `.hpi` 文件
4. 点击"上传"并重启Jenkins

#### 方式二：手动复制文件
1. 将 `.hpi` 文件复制到 `$JENKINS_HOME/plugins/` 目录
2. 重启Jenkins

#### 方式三：开发调试模式
```bash
mvn hpi:run
```
这将启动一个带有插件的Jenkins实例在 http://localhost:8080/jenkins

## ⏰ 时区配置（重要）

**预约构建功能依赖正确的时区设置！**

### Docker 环境（已配置）

使用本项目的 `docker-compose.yml` 已自动配置为上海时区 (Asia/Shanghai, UTC+8)。

### 非 Docker 环境

如果您在生产环境使用，请配置 Jenkins 时区：

```bash
# 方法 1: 修改 JAVA_OPTS（推荐）
JAVA_OPTS="-Duser.timezone=Asia/Shanghai"

# 方法 2: 设置环境变量
export TZ=Asia/Shanghai
```

### 验证时区

在 Jenkins Script Console 运行：

```groovy
println "时区: " + TimeZone.getDefault().getID()
println "当前时间: " + new Date()
```

📖 **详细配置**: 参见 [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md#⏰-时区配置)

## 使用说明

### 1. 启用插件

1. 进入Jenkins任务配置页面
2. 勾选"启用预约构建"选项
3. 保存配置

### 2. 添加预约构建

1. 进入任务页面，点击左侧菜单的"预约构建"
2. 在"添加新预约"区域：
   - 选择预约执行的日期和时间
   - 填写预约描述（可选）
   - 如果任务有参数，设置各参数的值
3. 点击"添加预约"按钮

### 3. 管理预约

在预约构建页面可以：
- 查看所有待执行的预约及倒计时
- 取消未执行的预约
- 查看预约历史记录（已执行、已取消、已过期）
- 删除不需要的历史记录

### 4. 预约执行

到达预约时间后，插件会自动触发构建，并传入设置的参数。构建历史中会显示"预约构建"作为触发原因。

## 使用场景

### 场景1：定期发布部署
```
任务：生产环境部署
预约时间：每周五 18:00
参数：
  - ENVIRONMENT=production
  - BRANCH=release/v2.0
  - NOTIFY=true
```

### 场景2：夜间批量测试
```
任务：集成测试
预约1：今晚 23:00 - 参数 TEST_SUITE=smoke
预约2：今晚 23:30 - 参数 TEST_SUITE=regression
预约3：明早 02:00 - 参数 TEST_SUITE=performance
```

### 场景3：定时数据处理
```
任务：数据导出
预约时间：下周一 08:00
参数：
  - START_DATE=2024-01-01
  - END_DATE=2024-01-31
  - FORMAT=csv
```

## 配置示例

### 参数化任务配置

如果你的Jenkins任务使用了参数化构建，在添加预约时会自动显示所有参数供配置。

支持的参数类型：
- 字符串参数 (String Parameter)
- 布尔参数 (Boolean Parameter)
- 选择参数 (Choice Parameter)

### 权限要求

使用预约构建功能需要以下权限：
- `Job.READ` - 查看预约列表
- `Job.BUILD` - 添加、取消预约

## 技术细节

### 架构设计

```
┌─────────────────────────────────────┐
│   ScheduledBuildAction (Web UI)    │
│   - 添加预约                          │
│   - 取消预约                          │
│   - 查看预约列表                       │
└─────────────┬───────────────────────┘
              │
              ↓
┌─────────────────────────────────────┐
│   ScheduledBuildManager             │
│   - 管理所有预约任务                   │
│   - 持久化存储                        │
│   - 调度执行                          │
└─────────────┬───────────────────────┘
              │
              ↓
┌─────────────────────────────────────┐
│   ScheduledExecutorService          │
│   - 定时任务调度                       │
│   - 触发Jenkins构建                   │
└─────────────────────────────────────┘
```

### 数据模型

每个预约任务包含：
- ID：唯一标识符
- 任务名称：Jenkins任务的全路径名
- 预约时间：Unix时间戳
- 参数：Map<String, String>
- 描述：可选的说明文字
- 状态：已取消/已执行标志

### 持久化

预约数据通过Jenkins的配置持久化机制保存在：
```
$JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
```

## 常见问题

### Q: Jenkins重启后预约会丢失吗？
A: 不会。所有预约都会持久化保存，Jenkins重启后会自动恢复并重新调度。

### Q: 可以预约多久之后的构建？
A: 没有时间限制，可以预约任意未来时间的构建。

### Q: 预约过期了但没执行怎么办？
A: 如果Jenkins在预约时间处于关闭状态，该预约会标记为"已过期"，不会自动执行。需要手动重新添加预约。

### Q: 可以批量添加预约吗？
A: 当前版本需要逐个添加。如需批量操作，可以通过Jenkins API调用实现。

### Q: 支持循环预约吗（如每天、每周）？
A: 当前版本不支持循环预约。建议使用Jenkins内置的"定时构建"功能来实现循环任务。本插件专注于"一次性"的预约构建。

## 📚 文档

- **[快速开始](docs/QUICKSTART.md)** - 5分钟快速上手
- **[安装指南](docs/INSTALLATION_GUIDE.md)** - 详细安装步骤  
- **[使用说明](docs/USAGE_CN.md)** - 完整功能说明
- **[架构设计](docs/ARCHITECTURE.md)** - 系统架构文档
- **[贡献指南](docs/CONTRIBUTING.md)** - 如何参与开发
- **[故障排查](docs/TROUBLESHOOTING.md)** - 常见问题解决
- **[变更日志](docs/CHANGELOG.md)** - 版本更新记录
- **[完整文档列表](docs/)** - 查看所有文档

## 🔧 脚本工具

- **[scripts/build.sh](scripts/)** - 构建插件 HPI
- **[scripts/start-jenkins.sh](scripts/)** - 启动测试环境
- **[scripts/update-plugin.sh](scripts/)** - 热更新插件
- **[scripts/verify-installation.groovy](scripts/)** - 验证安装
- **[完整脚本列表](scripts/)** - 查看所有工具

## 开发

### 项目结构

```
jenkins-scheduled-build-plugin/
├── pom.xml                          # Maven 配置
├── docker-compose.yml               # Docker 测试环境
├── Jenkinsfile                      # CI/CD 流水线
├── docs/                            # 📚 所有文档
│   ├── README.md                    # 文档索引
│   ├── QUICKSTART.md                # 快速开始
│   ├── INSTALLATION_GUIDE.md        # 安装指南
│   ├── ARCHITECTURE.md              # 架构文档
│   ├── CONTRIBUTING.md              # 贡献指南
│   └── ...                          # 更多文档
├── scripts/                         # 🔧 开发脚本
│   ├── README.md                    # 脚本说明
│   ├── build.sh                     # 构建脚本
│   ├── start-jenkins.sh             # 启动测试环境
│   ├── update-plugin.sh             # 更新插件
│   └── ...                          # 更多脚本
├── src/
│   └── main/
│       ├── java/
│       │   └── io/jenkins/plugins/scheduledbuild/
│       │       ├── ScheduledBuildTask.java       # 数据模型
│       │       ├── ScheduledBuildManager.java    # 任务管理器
│       │       ├── ScheduledBuildProperty.java   # 任务属性
│       │       ├── ScheduledBuildAction.java     # Web 界面
│       │       └── PluginImpl.java              # 插件初始化
│       └── resources/
│           ├── index.jelly                       # 插件描述
│           └── io/jenkins/plugins/scheduledbuild/
│               ├── Messages.properties           # 国际化资源
│               ├── Messages_zh_CN.properties     # 中文资源
│               ├── ScheduledBuildProperty/
│               │   ├── config.jelly             # 配置界面
│               │   └── help.html                # 帮助文档
│               └── ScheduledBuildAction/
│                   └── index.jelly              # 主页面
├── init-scripts/                    # Jenkins 初始化脚本
│   ├── 01-create-admin-user.groovy  # 创建管理员
│   └── 02-create-demo-job.groovy    # 创建演示任务
├── .github/
│   └── workflows/                   # GitHub Actions
│       ├── ci.yml                   # 持续集成
│       ├── pr-check.yml             # PR 检查
│       └── release.yml              # 发布流程
└── README.md                        # 项目主页
```

### 构建插件

```bash
./scripts/build.sh
```

### 启动测试环境

```bash
./scripts/start-jenkins.sh
```

### 运行测试

```bash
mvn test
```

### 代码规范

项目遵循 Jenkins 插件开发的最佳实践。详见 [贡献指南](docs/CONTRIBUTING.md)。

## 贡献

欢迎提交 Issue 和 Pull Request！

- 📖 阅读 [贡献指南](docs/CONTRIBUTING.md)
- 🐛 提交 [Bug 报告](https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues/new)
- 💡 提出 [功能建议](https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues/new)
- 🔀 发起 [Pull Request](https://github.com/zsjohny/jenkins-scheduled-build-plugin/pulls)

## 许可证

MIT License

## 作者

开发团队

## 更新日志

### v1.0.0 (2025-10-19)
- 首次发布
- 支持多任务预约构建
- 支持参数化构建
- 支持取消预约
- 持久化存储
- Web管理界面
- 双重扩展点注册机制
- 标准 Jenkins 插件架构

## 相关链接

- [Jenkins插件开发指南](https://www.jenkins.io/doc/developer/plugin-development/)
- [Jenkins插件教程](https://www.jenkins.io/doc/developer/tutorial/)

## ⭐ Star History

如果这个项目对你有帮助，请给我们一个 Star！⭐

[![Star History Chart](https://api.star-history.com/svg?repos=zsjohny/jenkins-scheduled-build-plugin&type=Date)](https://star-history.com/#zsjohny/jenkins-scheduled-build-plugin&Date)

### 项目统计

[![GitHub Repo Size](https://img.shields.io/github/repo-size/zsjohny/jenkins-scheduled-build-plugin)](https://github.com/zsjohny/jenkins-scheduled-build-plugin)
[![GitHub Code Size](https://img.shields.io/github/languages/code-size/zsjohny/jenkins-scheduled-build-plugin)](https://github.com/zsjohny/jenkins-scheduled-build-plugin)
[![GitHub Commit Activity](https://img.shields.io/github/commit-activity/m/zsjohny/jenkins-scheduled-build-plugin)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/commits/main)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/zsjohny/jenkins-scheduled-build-plugin)](https://github.com/zsjohny/jenkins-scheduled-build-plugin/commits/main)

### 社区

如果你喜欢这个项目：
- ⭐ 给项目一个 Star
- 🐛 [报告 Bug](https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues)
- 💡 [提交功能建议](https://github.com/zsjohny/jenkins-scheduled-build-plugin/issues)
- 🤝 [贡献代码](CONTRIBUTING.md)



