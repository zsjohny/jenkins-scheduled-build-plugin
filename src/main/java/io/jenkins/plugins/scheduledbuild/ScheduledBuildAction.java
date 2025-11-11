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
     * 获取单个预约任务（用于编辑）
     */
    public ScheduledBuildTask getTask(String taskId) {
        ScheduledBuildManager manager = ScheduledBuildManager.get();
        if (manager == null) {
            return null;
        }
        return manager.getTask(taskId);
    }
    
    /**
     * 获取周期性规则
     */
    public List<RecurringScheduleRule> getRecurringRules() {
        ScheduledBuildManager manager = ScheduledBuildManager.get();
        if (manager == null) {
            LOGGER.warning("ScheduledBuildManager 未初始化");
            return Collections.emptyList();
        }
        return manager.getRecurringRulesForJob(job.getFullName());
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

    /**
     * 更新预约构建
     */
    @POST
    public void doUpdate(StaplerRequest req, StaplerResponse rsp,
                        @QueryParameter("taskId") String taskId,
                        @QueryParameter("scheduledTime") String scheduledTime,
                        @QueryParameter("description") String description) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            // 获取原任务
            ScheduledBuildTask task = manager.getTask(taskId);
            if (task == null) {
                throw new IllegalArgumentException("任务不存在: " + taskId);
            }
            
            // 检查任务是否可编辑（只能编辑待执行的任务）
            if (!task.isPending()) {
                throw new IllegalArgumentException("只能编辑待执行的预约任务");
            }
            
            // 解析新时间
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

            // 更新任务
            boolean success = manager.updateScheduledBuild(taskId, timestamp, parameters, description);
            
            if (success) {
                LOGGER.info(String.format("用户 %s 更新了预约构建: %s",
                        getCurrentUser(), taskId));
            } else {
                throw new IllegalStateException("更新预约构建失败");
            }

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("更新预约构建失败: " + e.getMessage());
            throw new ServletException("更新预约构建失败: " + e.getMessage(), e);
        }
    }

    // ==================== 周期性规则 API ====================

    /**
     * 添加周期性规则
     */
    @POST
    public void doAddRecurringRule(StaplerRequest req, StaplerResponse rsp) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            // 获取基本参数
            String scheduleType = req.getParameter("scheduleType");
            String description = req.getParameter("recurringDescription");
            
            // 获取构建参数
            Map<String, String> parameters = new HashMap<>();
            for (ParameterDefinition param : getJobParameters()) {
                String value = req.getParameter("recurring_param_" + param.getName());
                if (value != null && !value.isEmpty()) {
                    parameters.put(param.getName(), value);
                } else if (param.getDefaultParameterValue() != null) {
                    parameters.put(param.getName(), 
                                 param.getDefaultParameterValue().getValue().toString());
                }
            }

            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }

            RecurringScheduleRule rule = null;
            
            switch (scheduleType) {
                case "DAILY":
                    String dailyTime = req.getParameter("dailyTime");
                    rule = RecurringScheduleRule.createDaily(job.getFullName(), dailyTime, parameters, description);
                    break;
                    
                case "WEEKLY":
                    String weeklyTime = req.getParameter("weeklyTime");
                    Set<Integer> weekDays = new HashSet<>();
                    for (int i = 1; i <= 7; i++) {
                        String dayParam = req.getParameter("weekDay" + i);
                        if ("on".equals(dayParam) || "true".equals(dayParam)) {
                            weekDays.add(i);
                        }
                    }
                    if (weekDays.isEmpty()) {
                        throw new IllegalArgumentException("请至少选择一个星期");
                    }
                    rule = RecurringScheduleRule.createWeekly(job.getFullName(), weekDays, weeklyTime, parameters, description);
                    break;
                    
                case "MONTHLY":
                    String monthlyTime = req.getParameter("monthlyTime");
                    String monthDaysStr = req.getParameter("monthDays");
                    Set<Integer> monthDays = new HashSet<>();
                    if (monthDaysStr != null && !monthDaysStr.isEmpty()) {
                        for (String day : monthDaysStr.split(",")) {
                            try {
                                int dayNum = Integer.parseInt(day.trim());
                                if (dayNum >= 1 && dayNum <= 31) {
                                    monthDays.add(dayNum);
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无效的日期
                            }
                        }
                    }
                    if (monthDays.isEmpty()) {
                        throw new IllegalArgumentException("请至少选择一个日期");
                    }
                    rule = RecurringScheduleRule.createMonthly(job.getFullName(), monthDays, monthlyTime, parameters, description);
                    break;
                    
                case "CRON":
                    String cronExpression = req.getParameter("cronExpression");
                    rule = RecurringScheduleRule.createCron(job.getFullName(), cronExpression, parameters, description);
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的调度类型: " + scheduleType);
            }

            manager.addRecurringRule(rule);

            LOGGER.info(String.format("用户 %s 为任务 %s 添加了周期性规则: %s",
                    getCurrentUser(), job.getFullName(), rule));

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("添加周期性规则失败: " + e.getMessage());
            throw new ServletException("添加周期性规则失败: " + e.getMessage(), e);
        }
    }

    /**
     * 启用/禁用周期性规则
     */
    @POST
    public void doToggleRecurringRule(StaplerRequest req, StaplerResponse rsp,
                                     @QueryParameter("ruleId") String ruleId,
                                     @QueryParameter("enabled") boolean enabled) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            boolean success = manager.toggleRecurringRule(ruleId, enabled);
            if (success) {
                LOGGER.info(String.format("用户 %s %s了周期性规则: %s",
                        getCurrentUser(), enabled ? "启用" : "禁用", ruleId));
            } else {
                throw new IllegalArgumentException("规则不存在");
            }

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("切换周期性规则状态失败: " + e.getMessage());
            throw new ServletException("切换周期性规则状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除周期性规则
     */
    @POST
    public void doDeleteRecurringRule(StaplerRequest req, StaplerResponse rsp,
                                     @QueryParameter("ruleId") String ruleId) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            boolean success = manager.removeRecurringRule(ruleId);
            if (success) {
                LOGGER.info(String.format("用户 %s 删除了周期性规则: %s",
                        getCurrentUser(), ruleId));
            }

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("删除周期性规则失败: " + e.getMessage());
            throw new ServletException("删除周期性规则失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新周期性规则
     */
    @POST
    public void doUpdateRecurringRule(StaplerRequest req, StaplerResponse rsp) 
            throws IOException, ServletException {
        
        checkPermission();

        try {
            String ruleId = req.getParameter("ruleId");
            String scheduleType = req.getParameter("scheduleType");
            String description = req.getParameter("recurringDescription");
            
            ScheduledBuildManager manager = ScheduledBuildManager.get();
            if (manager == null) {
                throw new IllegalStateException("ScheduledBuildManager 未初始化，请重启 Jenkins");
            }
            
            // 获取原规则
            RecurringScheduleRule oldRule = manager.getRecurringRule(ruleId);
            if (oldRule == null) {
                throw new IllegalArgumentException("规则不存在: " + ruleId);
            }
            
            // 获取构建参数
            Map<String, String> parameters = new HashMap<>();
            for (ParameterDefinition param : getJobParameters()) {
                String value = req.getParameter("recurring_param_" + param.getName());
                if (value != null && !value.isEmpty()) {
                    parameters.put(param.getName(), value);
                } else if (param.getDefaultParameterValue() != null) {
                    parameters.put(param.getName(), 
                                 param.getDefaultParameterValue().getValue().toString());
                }
            }

            RecurringScheduleRule newRule = null;
            
            switch (scheduleType) {
                case "DAILY":
                    String dailyTime = req.getParameter("dailyTime");
                    newRule = RecurringScheduleRule.createDaily(job.getFullName(), dailyTime, parameters, description);
                    break;
                    
                case "WEEKLY":
                    String weeklyTime = req.getParameter("weeklyTime");
                    Set<Integer> weekDays = new HashSet<>();
                    for (int i = 1; i <= 7; i++) {
                        String dayParam = req.getParameter("weekDay" + i);
                        if ("on".equals(dayParam) || "true".equals(dayParam)) {
                            weekDays.add(i);
                        }
                    }
                    if (weekDays.isEmpty()) {
                        throw new IllegalArgumentException("请至少选择一个星期");
                    }
                    newRule = RecurringScheduleRule.createWeekly(job.getFullName(), weekDays, weeklyTime, parameters, description);
                    break;
                    
                case "MONTHLY":
                    String monthlyTime = req.getParameter("monthlyTime");
                    String monthDaysStr = req.getParameter("monthDays");
                    Set<Integer> monthDays = new HashSet<>();
                    if (monthDaysStr != null && !monthDaysStr.isEmpty()) {
                        for (String day : monthDaysStr.split(",")) {
                            try {
                                int dayNum = Integer.parseInt(day.trim());
                                if (dayNum >= 1 && dayNum <= 31) {
                                    monthDays.add(dayNum);
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无效的日期
                            }
                        }
                    }
                    if (monthDays.isEmpty()) {
                        throw new IllegalArgumentException("请至少选择一个日期");
                    }
                    newRule = RecurringScheduleRule.createMonthly(job.getFullName(), monthDays, monthlyTime, parameters, description);
                    break;
                    
                case "CRON":
                    String cronExpression = req.getParameter("cronExpression");
                    newRule = RecurringScheduleRule.createCron(job.getFullName(), cronExpression, parameters, description);
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的调度类型: " + scheduleType);
            }

            // 删除旧规则并添加新规则
            manager.removeRecurringRule(ruleId);
            manager.addRecurringRule(newRule);

            LOGGER.info(String.format("用户 %s 更新了周期性规则: %s -> %s",
                    getCurrentUser(), oldRule, newRule));

            // 重定向回预约构建页面
            rsp.sendRedirect(".");
        } catch (Exception e) {
            LOGGER.warning("更新周期性规则失败: " + e.getMessage());
            throw new ServletException("更新周期性规则失败: " + e.getMessage(), e);
        }
    }

    private void checkPermission() {
        job.checkPermission(Item.BUILD);
    }

    /**
     * 检查当前用户是否有BUILD权限
     * 用于UI层控制按钮显示
     */
    public boolean hasPermission() {
        return job.hasPermission(Item.BUILD);
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

