package io.jenkins.plugins.scheduledbuild;

import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import hudson.model.BooleanParameterValue;

import java.io.Serializable;
import java.util.*;

/**
 * 预约构建任务数据模型
 * 表示单个预约的构建任务
 */
public class ScheduledBuildTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String jobName;
    private final long scheduledTime;
    private final Map<String, String> parameters;
    private final String description;
    private boolean cancelled;
    private boolean executed;
    
    // 关联的周期性规则ID（如果是由周期性规则生成的任务）
    private String recurringRuleId;

    public ScheduledBuildTask(String jobName, long scheduledTime, Map<String, String> parameters, String description) {
        this.id = UUID.randomUUID().toString();
        this.jobName = jobName;
        this.scheduledTime = scheduledTime;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.description = description;
        this.cancelled = false;
        this.executed = false;
        this.recurringRuleId = null;
    }
    
    /**
     * 构造函数：从周期性规则创建任务
     */
    public ScheduledBuildTask(String jobName, long scheduledTime, Map<String, String> parameters, 
                             String description, String recurringRuleId) {
        this.id = UUID.randomUUID().toString();
        this.jobName = jobName;
        this.scheduledTime = scheduledTime;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.description = description;
        this.cancelled = false;
        this.executed = false;
        this.recurringRuleId = recurringRuleId;
    }

    public String getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public Date getScheduledDate() {
        return new Date(scheduledTime);
    }

    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }

    public String getDescription() {
        return description;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public boolean isPending() {
        return !cancelled && !executed && scheduledTime > System.currentTimeMillis();
    }

    public boolean isExpired() {
        return !executed && scheduledTime <= System.currentTimeMillis();
    }
    
    public String getRecurringRuleId() {
        return recurringRuleId;
    }
    
    public void setRecurringRuleId(String recurringRuleId) {
        this.recurringRuleId = recurringRuleId;
    }
    
    /**
     * 是否由周期性规则生成
     */
    public boolean isFromRecurringRule() {
        return recurringRuleId != null;
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

    /**
     * 转换为Jenkins参数值列表
     */
    public List<ParameterValue> toParameterValues() {
        List<ParameterValue> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // 尝试判断参数类型
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                values.add(new BooleanParameterValue(key, Boolean.parseBoolean(value)));
            } else {
                values.add(new StringParameterValue(key, value));
            }
        }
        return values;
    }

    @Override
    public String toString() {
        return String.format("ScheduledBuildTask[id=%s, job=%s, time=%s, params=%s, cancelled=%s, executed=%s, recurringRule=%s]",
                id, jobName, new Date(scheduledTime), getParametersString(), cancelled, executed, recurringRuleId);
    }
}

