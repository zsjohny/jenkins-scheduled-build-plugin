package io.jenkins.plugins.scheduledbuild;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.OptionalJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Jenkins任务属性，启用预约构建功能
 */
public class ScheduledBuildProperty extends OptionalJobProperty<Job<?, ?>> {

    @DataBoundConstructor
    public ScheduledBuildProperty() {
    }

    @Override
    public ScheduledBuildPropertyDescriptor getDescriptor() {
        return (ScheduledBuildPropertyDescriptor) super.getDescriptor();
    }

    @Extension
    public static class ScheduledBuildPropertyDescriptor extends OptionalJobPropertyDescriptor {
        
        @Override
        public String getDisplayName() {
            return "启用预约构建";
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends Job> jobType) {
            // 适用于所有任务类型
            return true;
        }
    }
}



