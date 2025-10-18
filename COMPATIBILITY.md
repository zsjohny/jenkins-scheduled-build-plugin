# 兼容性说明

## Jenkins 版本兼容性

本插件已针对 **2022年的Jenkins版本** 进行优化，确保向后兼容。

### 最低要求版本
- **Jenkins**: 2.346.3 LTS (2022年6月发布)
- **Java**: 11

### 已测试版本
- ✅ Jenkins 2.346.3 LTS (2022年6月)
- ✅ Jenkins 2.361.4 LTS (2022年9月)
- ✅ 更高版本的Jenkins (理论兼容)

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Jenkins Core | 2.346.3+ | 最低支持版本 |
| Java | 11+ | 运行时要求 |
| Maven | 3.8+ | 构建工具 |
| Plugin Parent POM | 4.40 | Jenkins插件父POM |
| maven-hpi-plugin | 3.27 | Jenkins插件打包工具 |
| 插件依赖 | 无 | 使用Jenkins环境已有插件 ✅ |

## 构建说明

### 使用 Java 11 构建

由于项目针对Jenkins 2.346.3进行配置，建议使用Java 11进行构建：

```bash
# 设置Java 11（macOS）
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# 构建插件
mvn clean package -DskipTests
```

### 跳过测试

如果项目路径包含空格（如iCloud Drive路径），建议使用 `-DskipTests` 跳过测试：

```bash
mvn clean package -DskipTests
```

## 兼容性变更历史

### v1.0.0-SNAPSHOT (当前版本)
- ✅ 降低Jenkins最低要求版本至 2.346.3 LTS
- ✅ 使用Plugin Parent POM 4.40以支持较旧的Jenkins版本
- ✅ 简化依赖配置，移除冲突依赖
- ✅ 修复Java 23兼容性问题
- ✅ 修复代码linter错误

## 已知限制

1. **测试执行**: 由于路径空格问题，某些环境下测试可能失败，建议使用 `-DskipTests`
2. **Java版本**: 必须使用Java 11进行构建，不支持Java 8
3. **Jenkins版本**: 不支持2.346.3之前的Jenkins版本

## 升级建议

如果您的Jenkins版本低于2.346.3：

1. **推荐方案**: 升级Jenkins到2.346.3 LTS或更高版本
   - Jenkins 2.346.x 是稳定的LTS版本
   - 包含重要的安全更新和bug修复

2. **备选方案**: 如果必须使用更低版本，需要修改 `pom.xml`：
   ```xml
   <properties>
       <jenkins.version>YOUR_VERSION</jenkins.version>
   </properties>
   <parent>
       <version>COMPATIBLE_PARENT_VERSION</version>
   </parent>
   ```
   但这需要您自行测试兼容性。

## 支持与反馈

如果您在特定Jenkins版本上遇到兼容性问题，请通过以下方式反馈：

- 提交GitHub Issue
- 包含Jenkins版本、Java版本、错误日志等信息
- 我们会尽力提供帮助

## 更多信息

- [Jenkins LTS 发布历史](https://www.jenkins.io/changelog-stable/)
- [Jenkins 插件开发指南](https://www.jenkins.io/doc/developer/plugin-development/)
- [项目README](README.md)

