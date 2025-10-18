# Jenkins 预约构建插件 - 版本要求说明

## 📋 最低版本要求

| 组件 | 最低版本 | 推荐版本 |
|------|---------|---------|
| **Jenkins** | 2.401.3 LTS | 2.401.3+ LTS |
| **Java** | 11 | 11 |
| **Maven** | 3.8+ | 3.9+ |

## 🎯 为什么选择 Jenkins 2.401.3 LTS？

### 技术原因

**2.401.3 LTS（2023年8月发布）是支持完整扩展点自动发现机制的最低稳定版本：**

1. **扩展点索引自动加载**
   - Jenkins 2.401.x 系列完全支持 `META-INF/services/hudson.Extension` 索引文件
   - 插件的 `@Extension` 注解类能够被 Jenkins 自动发现和注册
   - 无需任何额外的初始化脚本或配置

2. **改进的插件类加载器**
   - 2.401.x 引入了改进的插件类加载机制
   - 更好的类隔离和依赖管理
   - 支持 `<pluginFirstClassLoader>true</pluginFirstClassLoader>` 配置

3. **稳定的 LTS 版本**
   - 作为 LTS（长期支持）版本，经过充分测试
   - 有良好的社区支持和安全更新
   - 在生产环境中被广泛使用

### 旧版本的限制

#### Jenkins 2.346.3 及更早版本（不推荐）

**问题：**
- `META-INF/services/hudson.Extension` 索引文件存在但不会被自动读取
- `TransientActionFactory` 等扩展点需要手动注册
- 需要额外的初始化脚本（Groovy init script）来手动注册扩展点

**影响：**
- 增加部署复杂度
- 需要维护额外的初始化脚本
- 可能出现扩展点加载时序问题

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

### 扩展点索引文件

插件包含手动创建的扩展点索引文件：

**`src/main/resources/META-INF/services/hudson.Extension`**
```
io.jenkins.plugins.scheduledbuild.ScheduledBuildManager
io.jenkins.plugins.scheduledbuild.ScheduledBuildAction$ScheduledBuildActionFactory
io.jenkins.plugins.scheduledbuild.ScheduledBuildProperty$ScheduledBuildPropertyDescriptor
```

### Jenkins 2.401.3+ 的自动发现流程

1. **插件加载阶段**
   - Jenkins 读取 `.hpi` 文件
   - 解压插件到工作目录
   
2. **类加载阶段**
   - 创建插件专用的类加载器
   - 加载 `META-INF/MANIFEST.MF`
   
3. **扩展点扫描阶段**
   - 读取 `META-INF/services/hudson.Extension`
   - 自动实例化所有列出的扩展点类
   - 注册到相应的扩展点列表
   
4. **扩展点可用**
   - `TransientActionFactory` 自动为所有任务添加 Action
   - `GlobalConfiguration` 自动注册管理器
   - 无需任何额外配置

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

