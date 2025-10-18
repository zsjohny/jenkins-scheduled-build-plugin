# Jenkins 预约构建插件 - 架构设计文档

## 概述

本插件为Jenkins提供预约构建功能，允许用户为任务设置多条定时构建任务，每个任务支持不同的参数配置，并可在到期前取消。

## 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Jenkins Web UI                        │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         ScheduledBuildAction (index.jelly)             │ │
│  │  - 添加预约表单                                          │ │
│  │  - 预约列表展示                                          │ │
│  │  - 取消/删除操作                                         │ │
│  └──────────────────────┬─────────────────────────────────┘ │
└─────────────────────────┼─────────────────────────────────────┘
                          │ HTTP POST
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                  ScheduledBuildAction                        │
│                     (Action层 - 控制器)                      │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  doSchedule()  - 处理添加预约请求                       │ │
│  │  doCancel()    - 处理取消预约请求                       │ │
│  │  doDelete()    - 处理删除记录请求                       │ │
│  │  getPendingBuilds() - 获取待执行预约                    │ │
│  └──────────────────────┬─────────────────────────────────┘ │
└─────────────────────────┼─────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│               ScheduledBuildManager                          │
│                  (服务层 - 业务逻辑)                         │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  addScheduledBuild()    - 添加预约任务                  │ │
│  │  cancelScheduledBuild() - 取消预约任务                  │ │
│  │  getTasksForJob()       - 获取任务的预约列表            │ │
│  │  scheduleTask()         - 调度任务执行                  │ │
│  │  executeTask()          - 执行构建                      │ │
│  └──────────────────────┬─────────────────────────────────┘ │
│                         │                                     │
│  ┌──────────────────────┴─────────────────────────────────┐ │
│  │  ConcurrentHashMap<String, ScheduledBuildTask>         │ │
│  │  存储所有预约任务                                        │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────┼─────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│           ScheduledExecutorService (Java并发)                │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  线程池 (5个线程)                                        │ │
│  │  - 管理定时任务                                          │ │
│  │  - 到期时触发executeTask()                              │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────┼─────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    Jenkins Core API                          │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Jenkins.getQueue().schedule2()                         │ │
│  │  - 触发构建                                              │ │
│  │  - 传递参数 (ParametersAction)                          │ │
│  │  - 记录触发原因 (CauseAction)                           │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. ScheduledBuildTask (数据模型)

**职责**: 表示单个预约构建任务

**主要属性**:
- `id`: 唯一标识符 (UUID)
- `jobName`: Jenkins任务全路径名
- `scheduledTime`: 预约时间 (Unix时间戳)
- `parameters`: 构建参数 (Map<String, String>)
- `description`: 预约描述
- `cancelled`: 是否已取消
- `executed`: 是否已执行

**主要方法**:
- `isPending()`: 判断是否待执行
- `isExpired()`: 判断是否已过期
- `toParameterValues()`: 转换为Jenkins参数值列表

**状态流转**:
```
创建 → 待执行 → 已执行
              ↓
           已取消
              ↓
           已过期 (如果Jenkins关闭)
```

### 2. ScheduledBuildManager (管理器)

**职责**: 管理所有预约任务，负责持久化和调度

**关键特性**:
- `@Extension`: 注册为Jenkins全局配置
- `GlobalConfiguration`: 自动持久化数据
- 单例模式: 通过 `get()` 获取实例

**数据存储**:
```java
ConcurrentHashMap<String, ScheduledBuildTask> tasks
```
- 线程安全
- 快速查找
- 支持并发访问

**调度机制**:
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
```
- 5个工作线程
- 延迟执行任务
- 到期自动触发

**生命周期**:
1. **初始化**: 构造函数中调用 `load()` 加载持久化数据
2. **恢复**: `recoverPendingTasks()` 重新调度未完成任务
3. **运行**: 处理添加、取消、执行操作
4. **持久化**: 每次变更后调用 `save()` 保存

**持久化文件**:
```
$JENKINS_HOME/io.jenkins.plugins.scheduledbuild.ScheduledBuildManager.xml
```

### 3. ScheduledBuildProperty (任务属性)

**职责**: 为Jenkins任务添加预约构建功能开关

**类型**: `OptionalJobProperty`
- 可选属性
- 在任务配置页面显示复选框
- 启用后任务页面显示预约构建入口

**配置文件**: 
- `config.jelly`: 配置界面
- `help.html`: 帮助文档

### 4. ScheduledBuildAction (Web界面)

**职责**: 提供预约构建的Web操作界面

**URL路径**: `/job/{jobName}/scheduled-builds/`

**主要端点**:

| 方法 | 路径 | 功能 | HTTP方法 |
|------|------|------|----------|
| index | `/` | 显示预约列表和添加表单 | GET |
| doSchedule | `/schedule` | 添加新预约 | POST |
| doCancel | `/cancel` | 取消预约 | POST |
| doDelete | `/delete` | 删除记录 | POST |

**界面组成**:
1. 添加预约表单
2. 待执行预约列表 (带倒计时)
3. 全部预约历史记录

**权限检查**:
- `Job.READ`: 查看预约列表
- `Job.BUILD`: 添加/取消预约

**注册机制**:
```java
@Extension
public static class ScheduledBuildActionFactory extends TransientActionFactory<Job>
```
- 自动为启用预约功能的任务添加Action
- 通过 `ScheduledBuildProperty` 判断是否启用

## 数据流

### 添加预约流程

```
用户填写表单
    ↓
doSchedule() 接收请求
    ↓
解析时间和参数
    ↓
ScheduledBuildManager.addScheduledBuild()
    ↓
创建 ScheduledBuildTask 对象
    ↓
存入 tasks Map
    ↓
scheduleTask() 提交到线程池
    ↓
save() 持久化
    ↓
重定向到预约列表页面
```

### 执行预约流程

```
ScheduledExecutorService 到期触发
    ↓
executeTask() 被调用
    ↓
检查任务状态 (是否已取消/已执行)
    ↓
获取 Jenkins Job 对象
    ↓
准备构建参数 (ParametersAction)
    ↓
准备触发原因 (CauseAction)
    ↓
Jenkins.getQueue().schedule2() 触发构建
    ↓
标记 task.executed = true
    ↓
save() 持久化
```

### 取消预约流程

```
用户点击取消按钮
    ↓
doCancel() 接收请求
    ↓
ScheduledBuildManager.cancelScheduledBuild()
    ↓
检查任务是否待执行
    ↓
设置 task.cancelled = true
    ↓
save() 持久化
    ↓
重定向到预约列表页面
```

## 并发控制

### 线程安全措施

1. **ConcurrentHashMap**: 任务存储使用线程安全的Map
2. **synchronized**: 关键方法使用同步锁
   - `addScheduledBuild()`
   - `cancelScheduledBuild()`
   - `removeTask()`
3. **双重检查**: executeTask() 中二次确认状态

```java
private void executeTask(ScheduledBuildTask task) {
    synchronized (this) {
        // 再次检查状态，防止并发问题
        if (task.isCancelled() || task.isExecuted()) {
            return;
        }
    }
    // 执行构建...
}
```

### 线程池配置

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
```

- 5个工作线程
- 支持同时执行多个预约
- 延迟调度 (`schedule()`方法)

## 持久化机制

### 使用Jenkins配置持久化

```java
@Extension
public class ScheduledBuildManager extends GlobalConfiguration {
    // 自动序列化/反序列化
}
```

### 序列化要求

所有数据类必须实现 `Serializable`:
- `ScheduledBuildTask implements Serializable`
- 所有字段类型必须可序列化

### 持久化时机

- 添加预约: `save()`
- 取消预约: `save()`
- 删除任务: `save()`
- 任务执行完成: `save()`

### 恢复机制

```java
public ScheduledBuildManager() {
    load();  // 从磁盘加载数据
    recoverPendingTasks();  // 重新调度未完成任务
}
```

## 参数化构建支持

### 参数类型映射

| Jenkins参数类型 | 处理方式 |
|----------------|---------|
| StringParameter | 直接映射 String |
| BooleanParameter | 解析 "true"/"false" |
| ChoiceParameter | 从选项列表选择 |

### 参数传递

```java
List<ParameterValue> parameterValues = task.toParameterValues();
ParametersAction parametersAction = new ParametersAction(parameterValues);
Jenkins.get().getQueue().schedule2(job, 0, actions);
```

## 扩展点

### 可扩展的地方

1. **参数类型支持**: 添加更多参数类型
2. **通知机制**: 预约执行前/后发送通知
3. **权限细化**: 区分添加/取消的权限
4. **API接口**: 提供REST API
5. **循环预约**: 支持重复执行的预约
6. **预约模板**: 保存常用预约配置

### 示例: 添加邮件通知

```java
private void executeTask(ScheduledBuildTask task) {
    // ... 执行前
    sendNotification("预约构建即将执行: " + task.getDescription());
    
    // 执行构建
    Queue.Item item = Jenkins.get().getQueue().schedule2(...);
    
    // ... 执行后
    if (item != null) {
        sendNotification("预约构建已触发: " + task.getDescription());
    }
}
```

## 性能考虑

### 内存使用

- 所有预约任务保存在内存中
- 建议定期清理历史记录
- 提供 `cleanupOldTasks()` 方法

### 调度效率

- 使用线程池避免频繁创建线程
- 延迟调度而非轮询
- 不会影响Jenkins核心性能

### 存储优化

- XML持久化文件大小取决于预约数量
- 建议控制在1000个以内
- 定期清理已完成任务

## 错误处理

### 各层错误处理策略

1. **Web层**: 捕获异常，显示友好错误信息
2. **服务层**: 记录日志，返回布尔值或抛出异常
3. **执行层**: 静默失败，记录详细日志

### 日志级别

- `INFO`: 正常操作（添加、取消、执行）
- `WARNING`: 非致命错误（任务不存在）
- `SEVERE`: 严重错误（执行失败、Jenkins未找到）

## 安全性

### 权限检查

```java
private void checkPermission() {
    job.checkPermission(Item.BUILD);
}
```

- 所有修改操作都需要 `Item.BUILD` 权限
- 查看操作需要 `Item.READ` 权限

### CSRF保护

- 所有POST请求自动受Jenkins CSRF保护
- 使用 `@POST` 注解声明

## 最佳实践

### 开发建议

1. **保持状态不可变**: 任务执行后不再修改
2. **日志记录**: 关键操作都记录日志
3. **原子操作**: 状态变更使用synchronized
4. **优雅降级**: 出错不影响Jenkins核心功能

### 测试建议

1. 测试并发添加预约
2. 测试Jenkins重启后恢复
3. 测试大量预约的性能
4. 测试参数传递正确性

## 未来改进

### 计划中的功能

1. **批量操作**: 批量添加/取消预约
2. **预约模板**: 保存和复用预约配置
3. **循环预约**: 支持每天/每周重复
4. **REST API**: 通过API管理预约
5. **WebSocket**: 实时更新倒计时
6. **统计报表**: 预约执行统计

### 技术债务

1. 当前不支持修改预约（需要先取消再添加）
2. 不记录预约创建人信息
3. 清理策略需要手动调用
4. 没有预约数量限制

## 总结

本插件采用经典的MVC架构：
- **Model**: ScheduledBuildTask
- **View**: Jelly视图文件
- **Controller**: ScheduledBuildAction
- **Service**: ScheduledBuildManager

设计重点：
- ✅ 线程安全
- ✅ 持久化可靠
- ✅ 用户界面友好
- ✅ 代码结构清晰
- ✅ 易于扩展



