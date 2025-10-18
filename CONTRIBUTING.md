# 贡献指南

感谢你考虑为Jenkins预约构建插件做出贡献！

## 如何贡献

### 报告Bug

如果你发现了Bug，请创建一个Issue，包含：

1. **清晰的标题**: 简要描述问题
2. **详细描述**: 
   - Jenkins版本
   - 插件版本
   - 重现步骤
   - 期望行为
   - 实际行为
3. **截图/日志**: 如果可能，提供截图或日志
4. **环境信息**:
   - 操作系统
   - Java版本
   - 浏览器（如果是UI问题）

### 提出新功能

欢迎提出新功能建议！请创建Issue说明：

1. **功能描述**: 你希望添加什么功能
2. **使用场景**: 为什么需要这个功能
3. **预期效果**: 功能应该如何工作
4. **替代方案**: 是否考虑过其他解决方案

### 提交代码

#### 准备工作

1. Fork本项目
2. 克隆你的Fork
```bash
git clone https://github.com/your-username/jenkins-scheduled-build-plugin.git
cd jenkins-scheduled-build-plugin
```

3. 创建分支
```bash
git checkout -b feature/your-feature-name
# 或
git checkout -b fix/bug-description
```

#### 开发规范

**代码风格**:
- 遵循Java代码规范
- 使用4空格缩进
- 类和方法添加JavaDoc注释
- 保持代码简洁易读

**命名规范**:
- 类名: `PascalCase`
- 方法名: `camelCase`
- 常量: `UPPER_SNAKE_CASE`
- 变量: `camelCase`

**注释规范**:
```java
/**
 * 添加预约构建任务
 * 
 * @param jobName 任务名称
 * @param scheduledTime 预约时间（Unix时间戳）
 * @param parameters 构建参数
 * @param description 预约描述
 * @return 创建的预约任务对象
 */
public ScheduledBuildTask addScheduledBuild(...) {
    // 实现代码
}
```

#### 测试要求

1. **单元测试**: 为新功能编写单元测试
2. **集成测试**: 测试与Jenkins的集成
3. **手动测试**: 在本地Jenkins实例中测试

运行测试:
```bash
mvn test
```

#### 提交规范

**Commit消息格式**:
```
<类型>: <简短描述>

<详细描述>

<相关Issue>
```

**类型**:
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具配置

**示例**:
```
feat: 添加批量取消预约功能

- 在预约列表页面添加全选框
- 添加批量取消按钮
- 实现批量取消逻辑

Closes #123
```

#### Pull Request流程

1. **确保测试通过**
```bash
mvn clean verify
```

2. **推送到你的Fork**
```bash
git push origin feature/your-feature-name
```

3. **创建Pull Request**
   - 访问原项目的GitHub页面
   - 点击"New Pull Request"
   - 选择你的分支
   - 填写PR描述

4. **PR描述模板**:
```markdown
## 变更说明
简要说明这个PR做了什么

## 变更类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 代码重构

## 测试
- [ ] 已添加单元测试
- [ ] 已在本地测试
- [ ] 所有测试通过

## 相关Issue
Closes #xxx

## 截图（如适用）
```

5. **代码审查**
   - 等待维护者审查
   - 根据反馈修改代码
   - 保持沟通

6. **合并**
   - 审查通过后会被合并
   - 你的贡献会出现在更新日志中

## 开发环境搭建

### 本地开发

1. **安装依赖**
   - JDK 11+
   - Maven 3.8+
   - Git

2. **克隆项目**
```bash
git clone https://github.com/zsjohny/jenkins-scheduled-build-plugin.git
cd jenkins-scheduled-build-plugin
```

3. **编译项目**
```bash
mvn clean compile
```

4. **运行开发模式**
```bash
mvn hpi:run
```
访问 http://localhost:8080/jenkins

5. **调试**
```bash
mvnDebug hpi:run
```
然后在IDE中连接到端口5005

### IDE配置

#### IntelliJ IDEA

1. Import Project → 选择pom.xml
2. 设置JDK 11
3. 安装Jenkins插件（可选）
4. 运行配置：
   - Main class: `org.jenkins.tools.test.maven.HpiRunMojo`
   - VM options: `-Xmx2g`

#### Eclipse

1. Import → Existing Maven Project
2. 设置JDK 11
3. Run As → Maven build
4. Goals: `hpi:run`

## 代码组织

### 目录结构
```
src/main/java/              Java源代码
  └── io/jenkins/plugins/scheduledbuild/
      ├── ScheduledBuildTask.java       数据模型
      ├── ScheduledBuildManager.java    业务逻辑
      ├── ScheduledBuildProperty.java   任务属性
      └── ScheduledBuildAction.java     Web控制器

src/main/resources/         资源文件
  ├── index.jelly                      插件描述
  └── io/jenkins/plugins/scheduledbuild/
      ├── Messages*.properties         国际化
      ├── ScheduledBuildAction/
      │   └── index.jelly             主页面
      └── ScheduledBuildProperty/
          ├── config.jelly            配置界面
          └── help.html               帮助文档
```

### 添加新功能的步骤

1. **在相应的类中添加方法**
2. **如果需要Web界面，添加Jelly文件**
3. **添加国际化消息**
4. **编写单元测试**
5. **更新文档**

## 文档贡献

文档同样重要！你可以：

1. **修正错误**: 发现文档中的错误？请修正
2. **补充说明**: 某个功能说明不清楚？请补充
3. **添加示例**: 提供更多使用示例
4. **翻译文档**: 将文档翻译成其他语言

文档文件：
- `README.md`: 项目概述
- `USAGE_CN.md`: 详细使用指南
- `QUICKSTART.md`: 快速开始
- `ARCHITECTURE.md`: 架构设计
- `CONTRIBUTING.md`: 本文件

## 代码审查清单

提交PR前，请确认：

- [ ] 代码遵循项目编码规范
- [ ] 添加了必要的注释
- [ ] 所有测试通过
- [ ] 没有引入新的告警
- [ ] 更新了相关文档
- [ ] Commit消息清晰明确
- [ ] 没有提交敏感信息
- [ ] 功能在本地测试通过

## 发布流程

（仅供维护者参考）

1. 更新版本号（pom.xml）
2. 更新CHANGELOG.md
3. 创建版本标签
4. 构建发布版本
5. 上传到Jenkins插件仓库
6. 发布GitHub Release

## 获取帮助

- **GitHub Issues**: 提问和讨论
- **邮件**: dev@jenkins.io
- **文档**: 查看项目文档

## 行为准则

- 尊重他人
- 保持友好和专业
- 接受建设性批评
- 关注对社区最有利的事情

## 许可证

贡献的代码将遵循项目的MIT许可证。

---

再次感谢你的贡献！每一个改进都让这个项目变得更好。



