package io.jenkins.plugins.scheduledbuild;

import hudson.Plugin;
import hudson.init.Initializer;
import hudson.init.InitMilestone;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

import java.util.logging.Logger;

/**
 * 插件实现类
 * 在插件加载时手动注册扩展点，确保与所有 Jenkins 版本兼容
 */
public class PluginImpl extends Plugin {
    private static final Logger LOGGER = Logger.getLogger(PluginImpl.class.getName());

    @Override
    public void start() throws Exception {
        LOGGER.info("Jenkins 预约构建插件启动中...");
    }

    /**
     * 在 Jenkins 完全启动后注册扩展点
     * 这确保了即使扩展点索引文件未被自动读取，插件仍能正常工作
     */
    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void registerExtensions() {
        try {
            Jenkins jenkins = Jenkins.get();
            
            // 检查扩展点是否已注册
            TransientActionFactory<?> factory = jenkins.getExtensionList(TransientActionFactory.class)
                    .stream()
                    .filter(f -> f.getClass().getName().contains("ScheduledBuildActionFactory"))
                    .findFirst()
                    .orElse(null);
            
            if (factory == null) {
                // 手动注册扩展点
                factory = new ScheduledBuildAction.ScheduledBuildActionFactory();
                jenkins.getExtensionList(TransientActionFactory.class).add(factory);
                LOGGER.info("✅ 已注册 ScheduledBuildActionFactory 扩展点");
            } else {
                LOGGER.info("✅ ScheduledBuildActionFactory 已存在（自动注册成功）");
            }
            
            LOGGER.info("当前 TransientActionFactory 数量: " + 
                       jenkins.getExtensionList(TransientActionFactory.class).size());
            
        } catch (Exception e) {
            LOGGER.severe("注册扩展点失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

