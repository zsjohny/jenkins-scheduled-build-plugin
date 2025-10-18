package io.jenkins.plugins.scheduledbuild;

import hudson.Extension;
import hudson.model.*;
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
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public ScheduledBuildManager() {
        instance = this;
        load();
        // 启动时恢复所有未执行的任务
        recoverPendingTasks();
        LOGGER.info("ScheduledBuildManager 已初始化");
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

        try {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins == null) {
                LOGGER.severe("Jenkins实例未找到");
                return;
            }

            Job<?, ?> job = jenkins.getItemByFullName(task.getJobName(), Job.class);
            if (job == null) {
                LOGGER.severe("找不到任务: " + task.getJobName());
                return;
            }

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

