package io.jenkins.plugins.scheduledbuild;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * 周期性调度规则
 * 定义一个可重复执行的构建规则
 */
public class RecurringScheduleRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RecurringScheduleRule.class.getName());

    /**
     * 调度类型
     */
    public enum ScheduleType {
        DAILY("每天"),           // 每天执行
        WEEKLY("每周"),          // 每周特定几天执行
        MONTHLY("每月"),         // 每月特定几号执行
        CRON("Cron表达式");      // 使用Cron表达式

        private final String displayName;

        ScheduleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String id;
    private final String jobName;
    private final ScheduleType scheduleType;
    private final String description;
    private final Map<String, String> parameters;
    private boolean enabled;
    private final long createdTime;

    // 每天：执行时间（格式：HH:mm）
    private String dailyTime;

    // 每周：星期几（1=周一, 7=周日）
    private Set<Integer> weekDays;
    private String weeklyTime;

    // 每月：日期（1-31）
    private Set<Integer> monthDays;
    private String monthlyTime;

    // Cron表达式
    private String cronExpression;

    // 生效时间范围（可选）
    private Long startTime;  // null表示立即生效
    private Long endTime;    // null表示永不结束

    /**
     * 私有构造函数，通过工厂方法创建
     */
    private RecurringScheduleRule(String jobName, ScheduleType scheduleType, 
                                 Map<String, String> parameters, String description) {
        this.id = UUID.randomUUID().toString();
        this.jobName = jobName;
        this.scheduleType = scheduleType;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.description = description;
        this.enabled = true;
        this.createdTime = System.currentTimeMillis();
    }

    /**
     * 工厂方法：创建每天执行的规则
     */
    public static RecurringScheduleRule createDaily(String jobName, String dailyTime, 
                                                   Map<String, String> parameters, String description) {
        RecurringScheduleRule rule = new RecurringScheduleRule(jobName, ScheduleType.DAILY, parameters, description);
        rule.dailyTime = dailyTime;
        return rule;
    }

    /**
     * 工厂方法：创建每周执行的规则
     */
    public static RecurringScheduleRule createWeekly(String jobName, Set<Integer> weekDays, String weeklyTime,
                                                    Map<String, String> parameters, String description) {
        RecurringScheduleRule rule = new RecurringScheduleRule(jobName, ScheduleType.WEEKLY, parameters, description);
        rule.weekDays = new HashSet<>(weekDays);
        rule.weeklyTime = weeklyTime;
        return rule;
    }

    /**
     * 工厂方法：创建每月执行的规则
     */
    public static RecurringScheduleRule createMonthly(String jobName, Set<Integer> monthDays, String monthlyTime,
                                                     Map<String, String> parameters, String description) {
        RecurringScheduleRule rule = new RecurringScheduleRule(jobName, ScheduleType.MONTHLY, parameters, description);
        rule.monthDays = new HashSet<>(monthDays);
        rule.monthlyTime = monthlyTime;
        return rule;
    }

    /**
     * 工厂方法：创建Cron表达式规则
     */
    public static RecurringScheduleRule createCron(String jobName, String cronExpression,
                                                  Map<String, String> parameters, String description) {
        RecurringScheduleRule rule = new RecurringScheduleRule(jobName, ScheduleType.CRON, parameters, description);
        rule.cronExpression = cronExpression;
        return rule;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getDailyTime() {
        return dailyTime;
    }

    public Set<Integer> getWeekDays() {
        return weekDays != null ? new HashSet<>(weekDays) : null;
    }

    public String getWeeklyTime() {
        return weeklyTime;
    }

    public Set<Integer> getMonthDays() {
        return monthDays != null ? new HashSet<>(monthDays) : null;
    }

    public String getMonthlyTime() {
        return monthlyTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * 获取规则的可读描述
     */
    public String getScheduleDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(scheduleType.getDisplayName()).append(" - ");
        
        switch (scheduleType) {
            case DAILY:
                sb.append("每天 ").append(dailyTime);
                break;
            case WEEKLY:
                sb.append("每周 ").append(formatWeekDays()).append(" ").append(weeklyTime);
                break;
            case MONTHLY:
                sb.append("每月 ").append(formatMonthDays()).append(" 日 ").append(monthlyTime);
                break;
            case CRON:
                sb.append(cronExpression);
                break;
        }
        
        return sb.toString();
    }

    /**
     * 格式化星期几
     */
    private String formatWeekDays() {
        if (weekDays == null || weekDays.isEmpty()) {
            return "";
        }
        
        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        List<Integer> sortedDays = new ArrayList<>(weekDays);
        Collections.sort(sortedDays);
        
        StringBuilder sb = new StringBuilder();
        for (int day : sortedDays) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            sb.append(dayNames[day]);
        }
        return sb.toString();
    }

    /**
     * 格式化月份日期
     */
    private String formatMonthDays() {
        if (monthDays == null || monthDays.isEmpty()) {
            return "";
        }
        
        List<Integer> sortedDays = new ArrayList<>(monthDays);
        Collections.sort(sortedDays);
        
        StringBuilder sb = new StringBuilder();
        for (int day : sortedDays) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            sb.append(day);
        }
        return sb.toString();
    }

    /**
     * 计算下一次执行时间
     * @param fromTime 从哪个时间开始计算（通常是当前时间）
     * @return 下一次执行的时间戳，如果没有则返回null
     */
    public Long getNextExecutionTime(long fromTime) {
        if (!enabled) {
            return null;
        }

        // 检查生效时间范围
        if (startTime != null && fromTime < startTime) {
            fromTime = startTime;
        }
        if (endTime != null && fromTime >= endTime) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromTime);
        
        Long nextTime = null;
        
        switch (scheduleType) {
            case DAILY:
                nextTime = calculateNextDailyTime(cal);
                break;
            case WEEKLY:
                nextTime = calculateNextWeeklyTime(cal);
                break;
            case MONTHLY:
                nextTime = calculateNextMonthlyTime(cal);
                break;
            case CRON:
                nextTime = calculateNextCronTime(cal);
                break;
        }

        // 确保下一次执行时间在有效范围内
        if (nextTime != null && endTime != null && nextTime >= endTime) {
            return null;
        }

        return nextTime;
    }

    /**
     * 计算每天的下一次执行时间
     */
    private Long calculateNextDailyTime(Calendar fromCal) {
        if (dailyTime == null) {
            return null;
        }

        try {
            String[] parts = dailyTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar nextCal = (Calendar) fromCal.clone();
            nextCal.set(Calendar.HOUR_OF_DAY, hour);
            nextCal.set(Calendar.MINUTE, minute);
            nextCal.set(Calendar.SECOND, 0);
            nextCal.set(Calendar.MILLISECOND, 0);

            // 如果今天的时间已过，则设置为明天
            if (nextCal.getTimeInMillis() <= fromCal.getTimeInMillis()) {
                nextCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            return nextCal.getTimeInMillis();
        } catch (Exception e) {
            LOGGER.warning("解析每天执行时间失败: " + dailyTime + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 计算每周的下一次执行时间
     */
    private Long calculateNextWeeklyTime(Calendar fromCal) {
        if (weekDays == null || weekDays.isEmpty() || weeklyTime == null) {
            return null;
        }

        try {
            String[] parts = weeklyTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // 尝试未来7天内的每一天
            for (int i = 0; i < 7; i++) {
                Calendar nextCal = (Calendar) fromCal.clone();
                nextCal.add(Calendar.DAY_OF_MONTH, i);
                
                int dayOfWeek = nextCal.get(Calendar.DAY_OF_WEEK);
                // 转换为1-7（周一到周日）
                int dayNum = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;

                if (weekDays.contains(dayNum)) {
                    nextCal.set(Calendar.HOUR_OF_DAY, hour);
                    nextCal.set(Calendar.MINUTE, minute);
                    nextCal.set(Calendar.SECOND, 0);
                    nextCal.set(Calendar.MILLISECOND, 0);

                    if (nextCal.getTimeInMillis() > fromCal.getTimeInMillis()) {
                        return nextCal.getTimeInMillis();
                    }
                }
            }

            return null;
        } catch (Exception e) {
            LOGGER.warning("解析每周执行时间失败: " + weeklyTime + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 计算每月的下一次执行时间
     */
    private Long calculateNextMonthlyTime(Calendar fromCal) {
        if (monthDays == null || monthDays.isEmpty() || monthlyTime == null) {
            return null;
        }

        try {
            String[] parts = monthlyTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // 尝试当前月份的每一天
            Calendar nextCal = (Calendar) fromCal.clone();
            int maxDayOfMonth = nextCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int day : monthDays) {
                if (day > maxDayOfMonth) {
                    continue; // 跳过不存在的日期（如2月31日）
                }

                nextCal = (Calendar) fromCal.clone();
                nextCal.set(Calendar.DAY_OF_MONTH, day);
                nextCal.set(Calendar.HOUR_OF_DAY, hour);
                nextCal.set(Calendar.MINUTE, minute);
                nextCal.set(Calendar.SECOND, 0);
                nextCal.set(Calendar.MILLISECOND, 0);

                if (nextCal.getTimeInMillis() > fromCal.getTimeInMillis()) {
                    return nextCal.getTimeInMillis();
                }
            }

            // 如果当前月份没有合适的日期，尝试下一个月
            nextCal = (Calendar) fromCal.clone();
            nextCal.add(Calendar.MONTH, 1);
            nextCal.set(Calendar.DAY_OF_MONTH, 1);
            maxDayOfMonth = nextCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            List<Integer> sortedDays = new ArrayList<>(monthDays);
            Collections.sort(sortedDays);

            for (int day : sortedDays) {
                if (day <= maxDayOfMonth) {
                    nextCal.set(Calendar.DAY_OF_MONTH, day);
                    nextCal.set(Calendar.HOUR_OF_DAY, hour);
                    nextCal.set(Calendar.MINUTE, minute);
                    nextCal.set(Calendar.SECOND, 0);
                    nextCal.set(Calendar.MILLISECOND, 0);
                    return nextCal.getTimeInMillis();
                }
            }

            return null;
        } catch (Exception e) {
            LOGGER.warning("解析每月执行时间失败: " + monthlyTime + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 计算Cron表达式的下一次执行时间
     * 注意：这里使用简化实现，实际项目可能需要更完整的Cron解析器
     */
    private Long calculateNextCronTime(Calendar fromCal) {
        // TODO: 实现完整的Cron表达式解析
        // 这里暂时返回null，后续可以集成cronutils等库
        LOGGER.warning("Cron表达式支持尚未完全实现: " + cronExpression);
        return null;
    }

    /**
     * 获取参数的字符串表示
     */
    public String getParametersString() {
        if (parameters.isEmpty()) {
            return "无参数";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("RecurringScheduleRule[id=%s, job=%s, type=%s, schedule=%s, enabled=%s]",
                id, jobName, scheduleType, getScheduleDescription(), enabled);
    }
}

