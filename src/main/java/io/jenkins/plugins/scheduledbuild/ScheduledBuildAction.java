package io.jenkins.plugins.scheduledbuild;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.TransientActionFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * 预约构建的操作面板
 * 在任务页面添加预约构建的入口
 */
public class ScheduledBuildAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(ScheduledBuildAction.class.getName());
    
    private final Job<?, ?> job;

    public ScheduledBuildAction(Job<?, ?> job) {
        this.job = job;
    }

    @Override
    public String getIconFileName() {
        // 使用传统的图标格式，兼容所有Jenkins版本
        // notepad.png 是Jenkins自带的图标，确保显示
        return "notepad.png";
    }

    @Override
    public String getDisplayName() {
        return "预约构建";
    }

    @Override
    public String getUrlName() {
        return "scheduled-builds";
    }

    public Job<?, ?> getJob() {
        return job;
    }

    /**
     * 获取该任务的所有预约
     */
    public List<ScheduledBuildTask> getScheduledBuilds() {
        ScheduledBuildManager manager = ScheduledBuildManager.get();
        if (manager == null) {
            LOGGER.warning("ScheduledBuildManager 未初始化");
            return Collections.emptyList();
        }
        return manager.getTasksForJob(job.getFullName());
    }

    /**
     * 获取待执行的预约
     */
    public List<ScheduledBuildTask> getPendingBuilds() {
        ScheduledBuildManager manager = ScheduledBuildManager.get();
        if (manager == null) {
            LOGGER.warning("ScheduledBuildManager 未初始化");
            return Collections.emptyList();
        }
        return manager.getPendingTasks(job.getFullName());
    }

    /**
     * 获取任务的参数定义
     */
    public List<ParameterDefinition> getJobParameters() {
        List<ParameterDefinition> params = new ArrayList<>();
        ParametersDefinitionProperty property = 
            (ParametersDefinitionProperty) job.getProperty(ParametersDefinitionProperty.class);
        if (property != null) {
            params.addAll(property.getParameterDefinitions());
        }
        return params;
    }

    /**
     * 格式化日期时间
     */
    public String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    /**
     * 获取相对时间描述
     */
    public String getRelativeTime(long timestamp) {
        long diff = timestamp - System.currentTimeMillis();
        if (diff < 0) {
            return "已过期";
        }
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " 天后";
        } else if (hours > 0) {
            return hours + " 小时后";
        } else if (minutes > 0) {
            return minutes + " 分钟后";
        } else {
            return seconds + " 秒后";
        }
    }

    /**
     * 添加预约构建
     */
    @POST
    public void doSchedule(StaplerRequest req, StaplerResponse rsp,
                          @QueryParameter("scheduledTime") String scheduledTime,
                          @QueryParameter("description") String description) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            // 解析时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date scheduleDate = sdf.parse(scheduledTime);
            long timestamp = scheduleDate.getTime();

            // 验证时间
            if (timestamp <= System.currentTimeMillis()) {
                throw new IllegalArgumentException("预约时间必须在未来");
            }

            // 获取参数
            Map<String, String> parameters = new HashMap<>();
            for (ParameterDefinition param : getJobParameters()) {
                String value = req.getParameter("param_" + param.getName());
                if (value != null && !value.isEmpty()) {
                    parameters.put(param.getName(), value);
                } else if (param.getDefaultParameterValue() != null) {
                    parameters.put(param.getName(), 
                                 param.getDefaultParameterValue().getValue().toString());
                }
            }

            // 添加预约
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            ScheduledBuildTask task = manager.addScheduledBuild(job.getFullName(), timestamp, parameters, description);

            LOGGER.info(String.format("用户 %s 为任务 %s 添加了预约构建: %s",
                    getCurrentUser(), job.getFullName(), task));

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("添加预约构建失败: " + e.getMessage());
            throw new ServletException("添加预约构建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 取消预约构建
     */
    @POST
    public void doCancel(StaplerRequest req, StaplerResponse rsp,
                        @QueryParameter("taskId") String taskId) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            boolean success = manager.cancelScheduledBuild(taskId);
            if (success) {
                LOGGER.info(String.format("用户 %s 取消了预约构建: %s",
                        getCurrentUser(), taskId));
            } else {
                throw new IllegalArgumentException("无法取消该预约（可能已执行或不存在）");
            }

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("取消预约构建失败: " + e.getMessage());
            throw new ServletException("取消预约构建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除预约记录
     */
    @POST
    public void doDelete(StaplerRequest req, StaplerResponse rsp,
                        @QueryParameter("taskId") String taskId) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            boolean success = manager.removeTask(taskId);
            if (success) {
                LOGGER.info(String.format("用户 %s 删除了预约构建记录: %s",
                        getCurrentUser(), taskId));
            }

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("删除预约构建记录失败: " + e.getMessage());
            throw new ServletException("删除预约构建记录失败: " + e.getMessage(), e);
        }
    }

    private void checkPermission() {
        job.checkPermission(Item.BUILD);
    }

    private String getCurrentUser() {
        User current = User.current();
        return current != null ? current.getId() : "anonymous";
    }

    /**
     * 自动为所有任务添加预约构建功能
     * 所有Job都可以使用预约构建，无需额外配置
     */
    @Extension
    public static class ScheduledBuildActionFactory extends TransientActionFactory<Job<?, ?>> {
        private static final Logger LOGGER = Logger.getLogger(ScheduledBuildActionFactory.class.getName());
        
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Class<Job<?, ?>> type() {
            return (Class) Job.class;
        }

        @Override
        public Collection<? extends Action> createFor(Job<?, ?> target) {
            // 为所有任务自动添加预约构建功能，无需手动启用
            LOGGER.fine("添加预约构建功能到任务: " + target.getFullName());
            return Collections.singleton(new ScheduledBuildAction(target));
        }
    }
}

