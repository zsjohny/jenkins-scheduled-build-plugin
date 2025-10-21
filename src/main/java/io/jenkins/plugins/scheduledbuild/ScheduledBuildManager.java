package io.jenkins.plugins.scheduledbuild;

import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 预约构建任务管理器
 * 负责管理所有预约的构建任务
 */
@Extension
public class ScheduledBuildManager extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(ScheduledBuildManager.class.getName());
    
    // 使用单例模式，确保在任何情况下都能获取实例
    private static volatile ScheduledBuildManager instance;
    
    private final Map<String, ScheduledBuildTask> tasks = new ConcurrentHashMap<>();
    // transient 避免序列化线程池，会在构造函数和 readResolve 中初始化
    private transient ScheduledExecutorService scheduler;

    public ScheduledBuildManager() {
        instance = this;
        // 初始化调度器
        initScheduler();
        load();
        // 启动时恢复所有未执行的任务
        recoverPendingTasks();
        LOGGER.info("ScheduledBuildManager 已初始化");
    }
    
    /**
     * 初始化调度器
     */
    private void initScheduler() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(5);
            LOGGER.info("调度器已初始化");
        }
    }
    
    /**
     * 反序列化后的处理，确保 scheduler 被重新初始化
     */
    private Object readResolve() {
        initScheduler();
        return this;
    }

    public static ScheduledBuildManager get() {
        // 优先使用 GlobalConfiguration 方式
        ScheduledBuildManager manager = GlobalConfiguration.all().get(ScheduledBuildManager.class);
        if (manager != null) {
            return manager;
        }
        
        // 如果 GlobalConfiguration 方式失败，使用单例实例
        if (instance != null) {
            LOGGER.warning("通过单例模式获取 ScheduledBuildManager");
            return instance;
        }
        
        // 最后的尝试：手动创建实例（不推荐，但比返回 null 好）
        LOGGER.warning("ScheduledBuildManager 未找到，创建新实例");
        synchronized (ScheduledBuildManager.class) {
            if (instance == null) {
                instance = new ScheduledBuildManager();
            }
            return instance;
        }
    }

    /**
     * 添加预约构建任务
     */
    public synchronized ScheduledBuildTask addScheduledBuild(String jobName, long scheduledTime, 
                                                             Map<String, String> parameters, 
                                                             String description) {
        ScheduledBuildTask task = new ScheduledBuildTask(jobName, scheduledTime, parameters, description);
        tasks.put(task.getId(), task);
        
        // 调度任务
        scheduleTask(task);
        
        // 持久化
        save();
        
        LOGGER.info("添加预约构建任务: " + task);
        return task;
    }

    /**
     * 取消预约构建任务
     */
    public synchronized boolean cancelScheduledBuild(String taskId) {
        ScheduledBuildTask task = tasks.get(taskId);
        if (task != null && task.isPending()) {
            task.setCancelled(true);
            save();
            LOGGER.info("取消预约构建任务: " + task);
            return true;
        }
        return false;
    }

    /**
     * 获取任务
     */
    public ScheduledBuildTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 获取所有任务
     */
    public List<ScheduledBuildTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * 获取指定任务的所有预约
     */
    public List<ScheduledBuildTask> getTasksForJob(String jobName) {
        return tasks.values().stream()
                .filter(task -> task.getJobName().equals(jobName))
                .sorted(Comparator.comparingLong(ScheduledBuildTask::getScheduledTime))
                .collect(Collectors.toList());
    }

    /**
     * 获取待执行的任务
     */
    public List<ScheduledBuildTask> getPendingTasks(String jobName) {
        return tasks.values().stream()
                .filter(task -> task.getJobName().equals(jobName) && task.isPending())
                .sorted(Comparator.comparingLong(ScheduledBuildTask::getScheduledTime))
                .collect(Collectors.toList());
    }

    /**
     * 删除任务
     */
    public synchronized boolean removeTask(String taskId) {
        ScheduledBuildTask task = tasks.remove(taskId);
        if (task != null) {
            save();
            LOGGER.info("删除预约构建任务: " + task);
            return true;
        }
        return false;
    }

    /**
     * 调度任务执行
     */
    private void scheduleTask(ScheduledBuildTask task) {
        long delay = task.getScheduledTime() - System.currentTimeMillis();
        if (delay <= 0) {
            LOGGER.warning("任务已过期，不会被调度: " + task);
            return;
        }

        // 确保调度器已初始化
        initScheduler();
        scheduler.schedule(() -> executeTask(task), delay, TimeUnit.MILLISECONDS);
        LOGGER.info(String.format("已调度任务 %s，将在 %d 毫秒后执行", task.getId(), delay));
    }

    /**
     * 执行任务
     */
    private void executeTask(ScheduledBuildTask task) {
        synchronized (this) {
            if (task.isCancelled()) {
                LOGGER.info("任务已取消，跳过执行: " + task);
                return;
            }

            if (task.isExecuted()) {
                LOGGER.info("任务已执行，跳过: " + task);
                return;
            }
        }

        // 关键修复：在 SYSTEM 权限上下文中执行，确保可以访问所有任务
        // 这解决了在 ScheduledExecutorService 线程池中执行时无法访问 Jenkins 任务的问题
        // 使用 ACL.as(SYSTEM) 确保有完整的系统权限
        try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
            executeTaskWithAuth(task);
        }
    }
    
    /**
     * 在认证上下文中执行任务
     */
    private void executeTaskWithAuth(ScheduledBuildTask task) {
        try {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins == null) {
                LOGGER.severe("Jenkins实例未找到");
                return;
            }

            LOGGER.info(String.format("开始执行预约任务: %s，任务名称: %s", task.getId(), task.getJobName()));
            
            // 尝试多种方式查找任务，提高兼容性
            Job<?, ?> job = findJob(jenkins, task.getJobName());
            if (job == null) {
                LOGGER.severe(String.format("找不到任务: %s (已尝试多种查找方式)", task.getJobName()));
                return;
            }
            
            LOGGER.info(String.format("找到任务: %s (类型: %s)", task.getJobName(), job.getClass().getSimpleName()));

            // 准备构建参数
            List<ParameterValue> parameterValues = task.toParameterValues();
            ParametersAction parametersAction = null;
            if (!parameterValues.isEmpty()) {
                parametersAction = new ParametersAction(parameterValues);
            }

            // 触发构建
            CauseAction causeAction = new CauseAction(new ScheduledBuildCause(task));
            
            List<Action> actions = new ArrayList<>();
            actions.add(causeAction);
            if (parametersAction != null) {
                actions.add(parametersAction);
            }

            if (job instanceof hudson.model.Queue.Task) {
                hudson.model.Queue.Item item = Jenkins.get().getQueue().schedule2((hudson.model.Queue.Task) job, 0, actions).getItem();
                if (item != null) {
                    LOGGER.info("成功触发预约构建: " + task);
                    synchronized (this) {
                        task.setExecuted(true);
                        save();
                    }
                } else {
                    LOGGER.warning("触发预约构建失败: " + task);
                }
            } else {
                LOGGER.warning(String.format("任务 %s 不是 Queue.Task 类型，无法触发构建", task.getJobName()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "执行预约构建任务失败: " + task, e);
        }
    }

    /**
     * 恢复未完成的任务
     */
    private void recoverPendingTasks() {
        LOGGER.info("开始恢复未完成的预约任务...");
        int count = 0;
        for (ScheduledBuildTask task : tasks.values()) {
            if (task.isPending()) {
                scheduleTask(task);
                count++;
            }
        }
        LOGGER.info(String.format("成功恢复 %d 个预约任务", count));
    }

    /**
     * 清理已完成和已取消的任务（可选的维护操作）
     */
    public synchronized int cleanupOldTasks(long olderThanMillis) {
        long cutoffTime = System.currentTimeMillis() - olderThanMillis;
        List<String> toRemove = new ArrayList<>();
        
        for (ScheduledBuildTask task : tasks.values()) {
            if ((task.isExecuted() || task.isCancelled()) && task.getScheduledTime() < cutoffTime) {
                toRemove.add(task.getId());
            }
        }
        
        for (String taskId : toRemove) {
            tasks.remove(taskId);
        }
        
        if (!toRemove.isEmpty()) {
            save();
        }
        
        return toRemove.size();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        save();
        return true;
    }
    
    /**
     * 尝试多种方式查找任务，提高兼容性
     * 支持 FreeStyle、Pipeline、Folder 等各种任务类型
     */
    private Job<?, ?> findJob(Jenkins jenkins, String jobName) {
        if (jobName == null || jobName.isEmpty()) {
            LOGGER.warning("任务名称为空");
            return null;
        }
        
        LOGGER.info(String.format("开始查找任务: %s", jobName));
        
        // 方式1: 使用 getItemByFullName (标准方式，支持文件夹路径)
        try {
            Job<?, ?> job = jenkins.getItemByFullName(jobName, Job.class);
            if (job != null) {
                LOGGER.info(String.format("方式1成功: getItemByFullName 找到任务 %s", jobName));
                return job;
            }
        } catch (Exception e) {
            LOGGER.warning(String.format("方式1失败: getItemByFullName 查找 %s 出错: %s", jobName, e.getMessage()));
        }
        
        // 方式2: 使用 getItem (不支持路径，仅根目录)
        try {
            Item item = jenkins.getItem(jobName);
            if (item instanceof Job) {
                LOGGER.info(String.format("方式2成功: getItem 找到任务 %s", jobName));
                return (Job<?, ?>) item;
            }
        } catch (Exception e) {
            LOGGER.warning(String.format("方式2失败: getItem 查找 %s 出错: %s", jobName, e.getMessage()));
        }
        
        // 方式3: 尝试处理转义的任务名称（URL编码）
        try {
            String decodedName = java.net.URLDecoder.decode(jobName, "UTF-8");
            if (!decodedName.equals(jobName)) {
                LOGGER.info(String.format("尝试解码后的任务名: %s -> %s", jobName, decodedName));
                Job<?, ?> job = jenkins.getItemByFullName(decodedName, Job.class);
                if (job != null) {
                    LOGGER.info(String.format("方式3成功: 使用解码名称找到任务 %s", decodedName));
                    return job;
                }
            }
        } catch (Exception e) {
            LOGGER.warning(String.format("方式3失败: URL解码查找 %s 出错: %s", jobName, e.getMessage()));
        }
        
        // 方式4: 遍历所有任务查找精确匹配（最后的兜底方案）
        try {
            LOGGER.info("方式4: 开始遍历所有任务");
            for (Item item : jenkins.getAllItems()) {
                if (item instanceof Job) {
                    Job<?, ?> job = (Job<?, ?>) item;
                    String fullName = job.getFullName();
                    String name = job.getName();
                    
                    // 尝试匹配 fullName 或 name
                    if (fullName.equals(jobName) || name.equals(jobName)) {
                        LOGGER.info(String.format("方式4成功: 遍历找到任务 %s (fullName: %s, name: %s)", 
                            jobName, fullName, name));
                        return job;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning(String.format("方式4失败: 遍历查找 %s 出错: %s", jobName, e.getMessage()));
        }
        
        // 所有方式都失败，记录详细诊断信息
        LOGGER.severe(String.format("所有查找方式都失败，无法找到任务: %s", jobName));
        
        // 诊断信息：当前认证上下文
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                LOGGER.info(String.format("当前认证上下文: %s (权限: %s)", 
                    auth.getName(), 
                    auth.getAuthorities()));
            } else {
                LOGGER.warning("当前认证上下文为 null");
            }
        } catch (Exception e) {
            LOGGER.warning("获取认证上下文失败: " + e.getMessage());
        }
        
        // 诊断信息：Jenkins 环境
        LOGGER.info(String.format("Jenkins 类型: %s", jenkins.getClass().getName()));
        LOGGER.info(String.format("Jenkins 版本: %s", Jenkins.VERSION));
        LOGGER.info(String.format("当前线程: %s", Thread.currentThread().getName()));
        
        // 列出可用的任务
        LOGGER.info("可用的任务列表:");
        try {
            List<Item> allItems = jenkins.getAllItems();
            LOGGER.info(String.format("总共 %d 个项目", allItems.size()));
            
            int count = 0;
            int jobCount = 0;
            for (Item item : allItems) {
                if (item instanceof Job) {
                    jobCount++;
                    Job<?, ?> job = (Job<?, ?>) item;
                    if (count < 20) {
                        LOGGER.info(String.format("  - %s (类型: %s, fullName: %s)", 
                            job.getName(), job.getClass().getSimpleName(), job.getFullName()));
                        count++;
                    }
                }
            }
            
            if (jobCount == 0) {
                LOGGER.warning("未找到任何任务！可能是权限问题或在错误的上下文中执行");
            } else {
                LOGGER.info(String.format("总共 %d 个任务", jobCount));
                if (jobCount > 20) {
                    LOGGER.info("  ... (只显示前20个任务)");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "列出任务失败", e);
        }
        
        return null;
    }

    /**
     * 预约构建的触发原因
     */
    public static class ScheduledBuildCause extends Cause {
        private final String taskId;
        private final String description;

        public ScheduledBuildCause(ScheduledBuildTask task) {
            this.taskId = task.getId();
            this.description = task.getDescription();
        }

        @Override
        public String getShortDescription() {
            return "预约构建 (ID: " + taskId + (description != null ? ", " + description : "") + ")";
        }
    }
}

