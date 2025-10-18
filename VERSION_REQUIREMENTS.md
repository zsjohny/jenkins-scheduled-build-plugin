# Jenkins 预约构建插件 - 版本要求说明

## 📋 最低版本要求

| 组件 | 最低版本 | 推荐版本 |
|------|---------|---------|
| **Jenkins** | 2.401.3 LTS | 2.401.3+ LTS |
| **Java** | 11 | 11 |
| **Maven** | 3.8+ | 3.9+ |

## 🎯 为什么选择 Jenkins 2.401.3 LTS？

### 技术原因

**2.401.3 LTS（2023年8月发布）是推荐的最低版本：**

1. **现代化的插件架构**
   - 改进的插件类加载机制
   - 更好的类隔离和依赖管理
   - 支持 `<pluginFirstClassLoader>true</pluginFirstClassLoader>` 配置

2. **双重注册机制**
   本插件采用**双重注册机制**确保在所有版本中都能正常工作：
   
   **方式 1：扩展点索引文件**
   - 文件路径：`META-INF/services/hudson.Extension`
   - Jenkins 2.401.3+ 原生支持自动读取
   - 更老的版本可能不支持

   **方式 2：插件初始化器（主要方式）**
   ```java
   @Initializer(after = InitMilestone.JOB_LOADED)
   public static void registerExtensions() {
       // 检测扩展点是否已自动注册
       // 如未注册则手动注册
   }
   ```
   - 使用 Jenkins 标准的 `Plugin` 类和 `@Initializer` 注解
   - 在插件加载时自动执行
   - **无需任何外部 Groovy 脚本**
   - 纯 Java 实现，是 Jenkins 插件的标准机制

3. **稳定的 LTS 版本**
   - 作为 LTS（长期支持）版本，经过充分测试
   - 有良好的社区支持和安全更新
   - 在生产环境中被广泛使用

### 旧版本的情况

#### Jenkins 2.346.3 及更早版本

**现状：**
- `META-INF/services/hudson.Extension` 索引文件可能不会被自动读取
- 扩展点索引机制在某些版本中不稳定

**解决方案：**
- 本插件使用 `PluginImpl` 类的 `@Initializer` 方法
- 在插件加载时自动检测并注册扩展点
- **无需任何外部配置或脚本**
- 与新版本完全兼容

**优势：**
- ✅ 在所有版本中使用相同的部署流程
- ✅ 无需维护额外的初始化脚本
- ✅ 纯 Java 实现，标准 Jenkins 插件机制
- ✅ 自动检测，避免重复注册

#### Jenkins 2.319.3 及更早版本（不支持）

**问题：**
- 父 POM 版本限制导致依赖冲突
- 注解处理器兼容性问题
- 缺少现代化的扩展点发现机制

## 📊 版本对比

| Jenkins 版本 | 扩展点自动发现 | 需要初始化脚本 | Maven 父 POM | 推荐使用 |
|-------------|--------------|---------------|-------------|---------|
| 2.319.3 LTS | ❌ | ✅ | 4.27 | ❌ |
| 2.346.3 LTS | ⚠️ 部分支持 | ✅ | 4.40 | ⚠️ |
| 2.401.3 LTS | ✅ 完整支持 | ❌ | 4.75 | ✅ |
| 2.414.x LTS | ✅ 完整支持 | ❌ | 4.80+ | ✅ |

## 🔧 技术实现细节

### 双重注册机制

本插件采用两种方式确保扩展点被正确注册：

#### 方式 1：扩展点索引文件

**`src/main/resources/META-INF/services/hudson.Extension`**
```
io.jenkins.plugins.scheduledbuild.ScheduledBuildManager
io.jenkins.plugins.scheduledbuild.ScheduledBuildAction$ScheduledBuildActionFactory
io.jenkins.plugins.scheduledbuild.ScheduledBuildProperty$ScheduledBuildPropertyDescriptor
```

- Jenkins 2.401.3+ 可能自动读取此文件
- 作为第一层保障

#### 方式 2：插件初始化器（主要机制）

**`src/main/java/io/jenkins/plugins/scheduledbuild/PluginImpl.java`**
```java
public class PluginImpl extends Plugin {
    
    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void registerExtensions() {
        Jenkins jenkins = Jenkins.get();
        
        // 检查是否已自动注册
        boolean alreadyRegistered = jenkins
            .getExtensionList(TransientActionFactory.class)
            .stream()
            .anyMatch(f -> f.getClass().getName()
                .contains("ScheduledBuildActionFactory"));
        
        if (!alreadyRegistered) {
            // 手动注册扩展点
            TransientActionFactory<?> factory = 
                new ScheduledBuildAction.ScheduledBuildActionFactory();
            jenkins.getExtensionList(TransientActionFactory.class)
                   .add(factory);
        }
    }
}
```

**工作流程：**

1. **插件启动** - `Plugin.start()` 方法被调用
2. **初始化器执行** - `@Initializer` 方法在 `JOB_LOADED` 之后运行
3. **检测注册状态** - 检查扩展点是否已通过索引文件注册
4. **按需注册** - 如果未自动注册，则手动注册
5. **完成** - 扩展点可用，`TransientActionFactory` 为所有任务添加 Action

**优势：**
- ✅ 无需外部 Groovy 脚本
- ✅ 标准 Jenkins 插件机制
- ✅ 自动检测，避免重复注册
- ✅ 在所有 Jenkins 版本中一致工作

## ⚠️ 升级建议

如果您当前使用的是旧版本 Jenkins：

### 从 2.346.3 升级到 2.401.3

**好处：**
- ✅ 移除初始化脚本依赖
- ✅ 简化部署流程
- ✅ 提高可靠性

**步骤：**
1. 备份 Jenkins 配置和数据
2. 升级 Jenkins 到 2.401.3 LTS
3. 重新安装插件（无需初始化脚本）
4. 验证功能正常

### 从更早版本升级

建议直接升级到 2.401.3 LTS，不建议使用中间版本。

## 📝 验证方法

安装插件后，在 Jenkins Script Console 运行：

```groovy
import jenkins.model.*
import io.jenkins.plugins.scheduledbuild.*

println "=== 验证扩展点自动注册 ==="

def factories = Jenkins.get().getExtensionList(jenkins.model.TransientActionFactory.class)
def found = factories.any { it.class.name.contains("ScheduledBuildActionFactory") }

if (found) {
    println "✅ 扩展点已自动注册 - Jenkins 版本满足要求"
} else {
    println "❌ 扩展点未注册 - 请升级到 Jenkins 2.401.3+"
}

println "\nJenkins 版本: " + Jenkins.getVersion()
```

## 🎉 总结

**Jenkins 2.401.3 LTS 是本插件的最低推荐版本，因为：**

1. ✅ **自动扩展点发现** - 无需手动干预
2. ✅ **稳定可靠** - LTS 版本，生产环境验证
3. ✅ **现代化** - 支持最新的 Jenkins 插件开发标准
4. ✅ **简化部署** - 标准安装流程，无额外配置

使用此版本或更新版本，您可以获得最佳的插件使用体验！

